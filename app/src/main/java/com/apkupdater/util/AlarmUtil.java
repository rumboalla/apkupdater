package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.apkupdater.R;
import com.apkupdater.receiver.AlarmReceiver_;
import com.apkupdater.updater.UpdaterOptions;

import org.androidannotations.annotations.EBean;

import java.util.Calendar;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EBean(scope = EBean.Scope.Singleton)
public class AlarmUtil
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	AlarmManager alarmManager;
	PendingIntent alarmIntent;
	Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public AlarmUtil(
		Context context
	) {
		mContext = context;

		// Get the alarm manager
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Create the pending intent that will be called by the alarm
		Intent intent = new Intent(context, AlarmReceiver_.class);
		intent.setAction("com.apkupdater.alarm");
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setAlarmFromOptions(
	) {
		UpdaterOptions options = new UpdaterOptions(mContext);
		String alarm = options.getAlarmOption();
		long interval;

		if (alarm.equals(mContext.getString(R.string.alarm_daily))) {
			interval = AlarmManager.INTERVAL_DAY;
		} else if (alarm.equals(mContext.getString(R.string.alarm_weekly))) {
			interval = AlarmManager.INTERVAL_DAY * 7;
		} else {
			cancelAlarm();
			return;
		}

		setAlarm(interval);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setAlarm(
	    long interval
	) {
		// Configure the alarm
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alarmIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void cancelAlarm(
	) {
		alarmManager.cancel(alarmIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////