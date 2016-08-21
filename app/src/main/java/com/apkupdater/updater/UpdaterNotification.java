package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.apkupdater.R;
import com.apkupdater.activity.MainActivity_;

import java.util.concurrent.atomic.AtomicInteger;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterNotification
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mNotificationBuilder;
	private Context mContext;
	private int mMaxApps;
	private AtomicInteger mNumApps;
	private UpdaterOptions mOptions;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterNotification(
		Context context,
	    int maxApps
	) {
		mContext = context;
		mMaxApps = maxApps;
		mOptions = new UpdaterOptions(mContext);
		mNumApps = new AtomicInteger(0);

		// Check if we should do notifications
		if (!doNotification()) {
			return;
		}

		createNotification();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void increaseProgress(
	) {
		// Check if we should do notifications
		if (!doNotification()) {
			return;
		}

		updateNotification(mMaxApps, mNumApps.incrementAndGet());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void finishNotification(
		int found
	) {
		// Check if we should do notifications
		if (!doNotification()) {
			return;
		}

		String s = mContext.getString(R.string.notification_update_content_finished).replace("$1", String.valueOf(found));
		mNotificationBuilder.setProgress(0, 0, false);
		mNotificationBuilder.setContentTitle(mContext.getString(R.string.notification_update_title_finished));
		mNotificationBuilder.setContentText(s);
		mNotificationManager.notify(42, mNotificationBuilder.build());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void failNotification(
	) {
		// Check if we should do notifications
		if (!doNotification()) {
			return;
		}

		mNotificationBuilder.setProgress(0, 0, false);
		mNotificationBuilder.setContentTitle(mContext.getString(R.string.notification_update_title_failed));
		mNotificationBuilder.setContentText(null);
		mNotificationManager.notify(42, mNotificationBuilder.build());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean doNotification(
	) {
		if (mOptions.getNotificationOption().equals(mContext.getString(R.string.notification_always))) {
			return true;
		} else if (mOptions.getNotificationOption().equals(mContext.getString(R.string.notification_never))) {
			return false;
		} else {
			// TODO: Check for background option
			return false;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void createNotification(
	) {
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationBuilder = new NotificationCompat.Builder(mContext);
		mNotificationBuilder.setContentTitle(mContext.getString(R.string.notification_update_title));
		mNotificationBuilder.setSmallIcon(R.drawable.ic_update_white_24dp);
		mNotificationManager.notify(42, mNotificationBuilder.build());
		mNotificationBuilder.setContentIntent(createPendingIntent());
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
		updateNotification(mMaxApps, 0);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void updateNotification(
		int max,
		int progress
	) {
		mNotificationBuilder.setContentText(getNotificationProgressString(max, progress));
		mNotificationBuilder.setProgress(max, progress, false);
		mNotificationManager.notify(42, mNotificationBuilder.build());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private PendingIntent createPendingIntent(
	) {
		Intent notificationIntent = new Intent(mContext, MainActivity_.class);
		notificationIntent.setAction("android.intent.action.MAIN");
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		//notificationIntent.setComponent(new ComponentName(mContext.getPackageName(), MainActivity_.class.getName()));
		notificationIntent.putExtra("tab", 1);
		return PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String getNotificationProgressString(
		int max,
		int progress
	) {
		String s = mContext.getString(R.string.notification_update_content);
		s = s.replace("$1", String.valueOf(progress));
		s = s.replace("$2", String.valueOf(max));
		return s;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////