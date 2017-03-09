package com.apkupdater.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.apkupdater.R;
import com.apkupdater.event.UpdateFinalProgressEvent;
import com.apkupdater.event.UpdateProgressEvent;
import com.apkupdater.event.UpdateStartEvent;
import com.apkupdater.event.UpdateStopEvent;
import com.apkupdater.model.AppState;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.model.Update;
import com.apkupdater.updater.IUpdater;
import com.apkupdater.updater.UpdaterAPKMirror;
import com.apkupdater.updater.UpdaterAPKPure;
import com.apkupdater.updater.UpdaterNotification;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.updater.UpdaterStatus;
import com.apkupdater.updater.UpdaterUptodown;
import com.apkupdater.util.AlarmUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EService
public class UpdaterService
	extends IntentService
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean
	MyBus mBus;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	AppState mAppState;

	@Bean
	AlarmUtil mAlarmUtil;

	@Bean
	LogUtil mLogger;

	private final Lock mMutex = new ReentrantLock(true);
	private List<Update> mUpdates = new ArrayList<>();
	private UpdaterNotification mNotification;
	private boolean mIsFromAlarm = false;
	static public final String isFromAlarmExtra = "isFromAlarm";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterService(
	) {
		super(UpdaterService.class.getSimpleName());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public IUpdater createUpdater(
		String type,
		Context context,
		String s1,
		String s2
	) {
		switch (type) {
			case "APKMirror":
				return new UpdaterAPKMirror(context, s1, s2);
			case "APKPure":
				return new UpdaterAPKPure(context, s1, s2);
			case "Uptodown":
				return new UpdaterUptodown(context, s1, s2);
			default:
				return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void updateSource(
		Executor executor,
		final String type,
		final InstalledApp app,
		final Queue<Throwable> errors
	) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				IUpdater upd = createUpdater(type, getBaseContext(), app.getPname(), app.getVersion());
				if (upd.getResultStatus() == UpdaterStatus.STATUS_UPDATE_FOUND) {
					Update u = new Update(app, upd.getResultUrl(), upd.getResultVersion());
					mUpdates.add(u);
					mBus.post(new UpdateProgressEvent(u));
				} else if (upd.getResultStatus() == UpdaterStatus.STATUS_ERROR){
					errors.add(upd.getResultError());
				}
				mNotification.increaseProgress(mUpdates.size());
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void checkForUpdates(
	) {
		String exit_message;

		try {
			// Lock mutex to avoid multiple update requests
			if (!mMutex.tryLock()) {
				mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.already_updating)));
				return;
			}

			if (!ServiceUtil.isConnected(getBaseContext())) {
				// Post error
				mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.update_check_failed_no_internet)));

				// Reschedule for 15 min if this was alarm scheduled
				if (mIsFromAlarm) {
					mAlarmUtil.rescheduleAlarm();
				}

				return;
			}


			// Check if we have at least one update source
			UpdaterOptions options = new UpdaterOptions(getBaseContext());
			if (!options.useAPKMirror() && !options.useUptodown() && !options.useAPKPure()) {
				mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.update_no_sources)));
				mMutex.unlock();
				return;
			}

			mAppState.clearUpdates();

			// Send start event
			mBus.post(new UpdateStartEvent());

			// Retrieve installed apps
			List<InstalledApp> installedApps = mInstalledAppUtil.getInstalledApps(getBaseContext());

			// Create the notification
			int multiplier = (options.useAPKMirror() ? 1 : 0) + (options.useAPKPure() ? 1 : 0)  + (options.useUptodown() ? 1 : 0) ;
			mNotification = new UpdaterNotification(getBaseContext(), installedApps.size() * multiplier);

			// Create an executor with 10 threads to perform the requests
			ExecutorService executor = Executors.newFixedThreadPool(10);
			final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

			// Iterate through installed apps and check for updates
			for (final InstalledApp app: installedApps) {
				// Check if this app is on the ignore list
				if (options.getIgnoreList().contains(app.getPname())) {
					continue;
				}
				if (options.useAPKMirror()) {
					updateSource(executor, "APKMirror", app, errors);
				}
				if (options.useUptodown()) {
					updateSource(executor, "Uptodown", app, errors);
				}
				if (options.useAPKPure()) {
					updateSource(executor, "APKPure", app, errors);
				}
			}

			// Wait until all threads are done
			executor.shutdown();
			while (!executor.isTerminated()) {
				Thread.sleep(1);
			}

			// If we got some errors
			if (errors.size() > 0) {
				exit_message = getBaseContext().getString(R.string.update_finished_with_errors);
				exit_message = exit_message.replace("$1", String.valueOf(errors.size()));

				// Log the errors
				for (Throwable t : errors) {
					mLogger.log("UpdaterService", t.getMessage() == null ? "" : t.getMessage(), LogMessage.SEVERITY_ERROR);
				}
			} else {
				exit_message = getBaseContext().getString(R.string.update_finished);
			}

			// Notify that the update check is over
			mAppState.setUpdates(mUpdates);
			mNotification.finishNotification(mUpdates.size());
			mBus.post(new UpdateFinalProgressEvent(mUpdates));
			mBus.post(new UpdateStopEvent(exit_message));
			mMutex.unlock();
		} catch (Exception e) {
			exit_message = getBaseContext().getString(R.string.update_failed).replace("$1", e.getClass().getSimpleName());
			mLogger.log("UpdaterService", e.getMessage() == null ? "" : e.getMessage(), LogMessage.SEVERITY_ERROR);
			mBus.post(new UpdateStopEvent(exit_message));
			if (mNotification != null) {
				mNotification.failNotification();
			}
			mMutex.unlock();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onHandleIntent(
		Intent intent
	) {
		try {
			mIsFromAlarm = intent.getExtras().getBoolean(isFromAlarmExtra);
		} catch (Exception ignored) {}

		checkForUpdates();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
