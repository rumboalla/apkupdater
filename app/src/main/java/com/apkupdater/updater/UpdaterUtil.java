package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.R;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.Update;
import com.apkupdater.util.InstalledAppUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EBean(scope = EBean.Scope.Singleton)
class UpdaterUtil
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RootContext
	Context context;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	private final Lock mMutex = new ReentrantLock(true);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void updateSource(
		Executor executor,
		final String type,
		final InstalledApp app,
	    final UpdaterCallback callback,
	    final Queue<Integer> errors
	) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				IUpdater upd = createUpdater(type, context, app.getPname(), app.getVersion());
				if (upd != null) {
					if (upd.getResultStatus() == UpdaterStatus.STATUS_UPDATE_FOUND) {
						callback.onUpdate(new Update(app, upd.getResultUrl(), upd.getResultVersion()));
					} else if (upd.getResultStatus() == UpdaterStatus.STATUS_ERROR) {
						errors.add(0);
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private IUpdater createUpdater(
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

	@Background(id="cancellable_task")
	public void checkForUpdates(
		final UpdaterCallback callback
	) {
		String exit_message;

		try {
			// Lock mutex to avoid multiple update requests
			if (!mMutex.tryLock()) {
				callback.onError(new Exception(context.getString(R.string.already_updating)));
				return;
			}

			// Notify of start of update process
			callback.onStart();

			// Check if we have at least one update source
			UpdaterOptions mOptions = new UpdaterOptions(context);
			if (!mOptions.useAPKMirror() && !mOptions.useUptodown() && !mOptions.useAPKPure()) {
				exit_message = context.getString(R.string.update_no_sources);
				callback.onFinish(exit_message);
				mMutex.unlock();
				return;
			}

			// Retrieve installed apps
			List<InstalledApp> installedApps = mInstalledAppUtil.getInstalledApps(context);

			// Create an executor with 10 threads to perform the requests
			ExecutorService executor = Executors.newFixedThreadPool(10);
			final ConcurrentLinkedQueue<Integer> errors = new ConcurrentLinkedQueue<>();

			// Iterate through installed apps and check for updates
			for (final InstalledApp app: installedApps) {
				// Check if this app is on the ignore list
				if (mOptions.getIgnoreList().contains(app.getPname())) {
					continue;
				}
				if (mOptions.useAPKMirror()) {
					updateSource(executor, "APKMirror", app, callback, errors);
				}
				if (mOptions.useUptodown()) {
					updateSource(executor, "Uptodown", app, callback, errors);
				}
				if (mOptions.useAPKPure()) {
					updateSource(executor, "APKPure", app, callback, errors);
				}
			}

			// Wait until all threads are done
			executor.shutdown();
			while (!executor.isTerminated()) {
				Thread.sleep(1);
			}

			// If we got some errors
			if (errors.size() > 0) {
				exit_message = context.getString(R.string.update_finished_with_errors);
				exit_message = exit_message.replace("$1", String.valueOf(errors.size()));
			} else {
				exit_message = context.getString(R.string.update_finished);
			}

			// Notify that the update check is over
			callback.onFinish(exit_message);
			mMutex.unlock();
		} catch (Exception e) {
			exit_message = context.getString(R.string.update_failed).replace("$1", e.getClass().getSimpleName());
			callback.onFinish(exit_message);
			mMutex.unlock();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
