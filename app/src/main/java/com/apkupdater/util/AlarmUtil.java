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
	Long mRescheduleTimeInMilis = 900000L;

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
		// Get update hour from options
		int hour = 12;
		try {
			UpdaterOptions options = new UpdaterOptions(mContext);
			String time = options.getUpdateHour();
			String [] split = time.split(":");
			hour = Integer.valueOf(split[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Configure the alarm
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		if (calendar.getTimeInMillis() < System.currentTimeMillis()     // Add a day if alarm is in the past
            || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == hour // Add a day if we are setting an alarm for the current hour
        ) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alarmIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void rescheduleAlarm(
	) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mRescheduleTimeInMilis, alarmIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void cancelAlarm(
	) {
		alarmManager.cancel(alarmIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////