package com.apkupdater.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.apkupdater.receiver.AlarmReceiver
import org.koin.core.KoinComponent
import java.util.Calendar

class AlarmUtil(private val context: Context, private val prefs: AppPreferences): KoinComponent {

	private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	private var pendingIntent: PendingIntent? = null

	fun setupAlarm(context: Context) = if (prefs.settings.checkForUpdates && pendingIntent == null) enableAlarm(context) else cancelAlarm()

	private fun enableAlarm(context: Context, hour: Int = 12, interval: Long = AlarmManager.INTERVAL_DAY) {
		pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

		val now = System.currentTimeMillis()
		val time = Calendar.getInstance().apply {
			timeInMillis = now
			set(Calendar.HOUR_OF_DAY, hour)
		}
		if (now > time.timeInMillis) time.add(Calendar.DAY_OF_MONTH, 1)

		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, interval, pendingIntent)
	}

	private fun cancelAlarm() = pendingIntent?.let { alarmManager.cancel(it) }

}