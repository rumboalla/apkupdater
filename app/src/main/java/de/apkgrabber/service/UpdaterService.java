package de.apkgrabber.service;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import de.apkgrabber.R;
import de.apkgrabber.adapter.UpdaterAdapter;
import de.apkgrabber.event.UpdateFinalProgressEvent;
import de.apkgrabber.event.UpdateProgressEvent;
import de.apkgrabber.event.UpdateStartEvent;
import de.apkgrabber.event.UpdateStopEvent;
import de.apkgrabber.model.AppState;
import de.apkgrabber.model.Constants;
import de.apkgrabber.model.InstalledApp;
import de.apkgrabber.model.LogMessage;
import de.apkgrabber.model.Update;
import de.apkgrabber.updater.IUpdater;
import de.apkgrabber.updater.UpdaterAPKMirrorAPI;
import de.apkgrabber.updater.UpdaterAPKPure;
import de.apkgrabber.updater.UpdaterAptoide;
import de.apkgrabber.updater.UpdaterGooglePlay;
import de.apkgrabber.updater.UpdaterNotification;
import de.apkgrabber.updater.UpdaterOptions;
import de.apkgrabber.updater.UpdaterStatus;
import de.apkgrabber.updater.UpdaterUptodown;
import de.apkgrabber.util.AlarmUtil;
import de.apkgrabber.util.GenericCallback;
import de.apkgrabber.util.InstalledAppUtil;
import de.apkgrabber.util.LogUtil;
import de.apkgrabber.util.MyBus;
import de.apkgrabber.util.NotificationHelper;
import de.apkgrabber.util.ServiceUtil;
import de.apkgrabber.util.VersionUtil;

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

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannel(Constants.UpdaterNotificationChannelId,
                "Updater", "", getBaseContext());
        NotificationHelper.createNotificationChannel(Constants.SelfUpdaterNotificationChannelId,
                "SelfUpdater", "", getBaseContext());
        NotificationHelper.createNotificationChannel(Constants.AutomaticUpdateNotificationChannelId,
                "AutomaticInstaller", "", getBaseContext());

		mNotification = new UpdaterNotification(getBaseContext(), 0);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(Constants.UpdaterNotificationId, mNotification.startUpdate());
		}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public IUpdater createUpdater(
		String type,
		Context context,
		InstalledApp app
	) {
		switch (type) {
			//case "GooglePlay":
			//	return new UpdaterGooglePlay(context, app.getPname(), String.valueOf(app.getVersionCode()));
			case "APKPure":
				return new UpdaterAPKPure(context, app.getPname(), app.getVersion());
			case "Uptodown":
				return new UpdaterUptodown(context, app.getPname(), app.getVersion());
			case "Aptoide":
				return new UpdaterAptoide(context, app.getPname(), app.getVersion());
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
				IUpdater upd = createUpdater(type, getBaseContext(), app);
				if (upd.getResultStatus() == UpdaterStatus.STATUS_UPDATE_FOUND) {
					Update u = new Update(app, upd.getResultUrl(), upd.getResultVersion(), upd.isBeta(), upd.getResultCookie(), upd.getResultVersionCode());
					mUpdates.add(u);
					mBus.post(new UpdateProgressEvent(u));
				} else if (upd.getResultStatus() == UpdaterStatus.STATUS_ERROR) {
					errors.add(upd.getResultError());
					mBus.post(new UpdateProgressEvent(null));
				} else {
					mBus.post(new UpdateProgressEvent(null));
				}
				mAppState.increaseUpdateProgress();
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

			// Check for connectivity
			if (!ServiceUtil.isConnected(getBaseContext())) {
				// Post error
				mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.update_check_failed_no_internet)));

				// Reschedule for 15 min if this was alarm scheduled
				if (mIsFromAlarm) {
					mAlarmUtil.rescheduleAlarm();
				}

				return;
			}

			// Get the options
			final UpdaterOptions options = new UpdaterOptions(getBaseContext());

			// Check if wifi only option
			if (options.getWifiOnly()) {
				if (!ServiceUtil.isWifi(getBaseContext())) {
					// Post error
					mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.update_check_failed_no_wifi)));
					return;
				}
			}

			// Check if we have at least one update source
			if (!options.useAPKMirror() && !options.useUptodown() && !options.useAPKPure() && !options.useGooglePlay() && !options.useAptoide()) {
				mBus.post(new UpdateStopEvent(getBaseContext().getString(R.string.update_no_sources)));
				mMutex.unlock();
				return;
			}

			mAppState.clearUpdates();
			mAppState.setUpdateProgress(0);

			// Retrieve installed apps
			List<InstalledApp> installedApps = mInstalledAppUtil.getInstalledApps(getBaseContext());

            // Remove ignored apps
            final List<InstalledApp> apps = new ArrayList<>();
            for (InstalledApp app : installedApps) {
                if (!options.getIgnoreList().contains(app.getPname())) {
                    apps.add(app);
                }
            }

            // Create an executor with N threads to perform the requests
			ExecutorService executor = Executors.newFixedThreadPool(options.getNumThreads());
			final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
            int appCount = 0;

			// Callback for APKMirrorAPI and GooglePlay
            final GenericCallback<Update> callback = new GenericCallback<Update>() {
                @Override
                public void onResult(Update u) {
                    if (u != null) {
                        mUpdates.add(u);
                        mBus.post(new UpdateProgressEvent(u));
                    } else {
                        mBus.post(new UpdateProgressEvent(null));
                    }

                    mAppState.increaseUpdateProgress();
                    mNotification.increaseProgress(mUpdates.size());
                }
            };

            if (options.useGooglePlay()) {
                appCount += apps.size();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        UpdaterGooglePlay updater = new UpdaterGooglePlay(
                            getBaseContext(),
                            apps,
                            Executors.newFixedThreadPool(options.getNumThreads()),
                            callback
                        );
                        if (updater.getResultStatus() == UpdaterStatus.STATUS_ERROR) {
                            errors.add(updater.getResultError());
                        }
                    }
                });
            }

			if (options.useAPKMirror()) {
				appCount += apps.size();
				// Split in batches of 100 and process
				for (final List<InstalledApp> batch : VersionUtil.batchList(apps, 100)) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							UpdaterAPKMirrorAPI upd = new UpdaterAPKMirrorAPI(getBaseContext(), batch, callback);
							if (upd.getResultStatus() == UpdaterStatus.STATUS_ERROR) {
								errors.add(upd.getResultError());
							}
						}
					});
				}
			}

            // Iterate through installed apps and check for updates
            for (final InstalledApp app: apps) {
                if (options.useUptodown()) {
                    appCount++;
                    updateSource(executor, "Uptodown", app, errors);
                }

                if (options.useAPKPure()) {
                    appCount++;
                    updateSource(executor, "APKPure", app, errors);
                }

                if (options.useAptoide()) {
					appCount++;
					updateSource(executor, "Aptoide", app, errors);
				}

            }

            mAppState.setUpdateMax(appCount);
            mNotification.setMaxApps(appCount);
            mBus.post(new UpdateStartEvent(appCount));

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

			// Clear update progress
			mAppState.setUpdateMax(0);
			mAppState.setUpdateProgress(0);

			// Filter updates
			mUpdates = UpdaterAdapter.Companion.sortUpdates(getBaseContext(), mUpdates);

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
		} finally {
            // Self update check
            SelfUpdateService.launchSelfUpdate(getApplicationContext());
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

	static public void checkForAppUpdates(
			Context context
	) {
		// app updates check
		if (new UpdaterOptions(context).checkUpdatesOnStartup()) {
			if (!ServiceUtil.isServiceRunning(context, UpdaterService_.class)) {
				UpdaterService_.intent(context).start();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
