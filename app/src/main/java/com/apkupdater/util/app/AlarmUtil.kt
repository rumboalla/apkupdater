package com.apkupdater.util.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.apkupdater.receiver.AlarmReceiver
import org.koin.core.KoinComponent
import java.util.Calendar

class AlarmUtil(private val context: Context, private val prefs: AppPrefs): KoinComponent {

	private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	private var pendingIntent: PendingIntent? = null

	fun setupAlarm(context: Context) = if (isEnabled()) enableAlarm(context) else cancelAlarm()

	private fun cancelAlarm() = pendingIntent?.let { alarmManager.cancel(it) }

	private fun enableAlarm(context: Context, interval: Long = getInterval()) {
		pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

		val now = System.currentTimeMillis()
		val time = Calendar.getInstance().apply { timeInMillis = now }

		when(prefs.settings.checkForUpdates) {
			"0" -> { // Daily
				setHour(time)
				if (time.timeInMillis < now) {
					time.add(Calendar.MILLISECOND, interval.toInt())
				}
			}
			"1" -> { // Weekly
				setHour(time)
				time.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
				if (time.timeInMillis < now) {
					time.add(Calendar.MILLISECOND, interval.toInt())
				}
			}
			else -> {
				setHour(time, time.get(Calendar.HOUR_OF_DAY))
				time.add(Calendar.MILLISECOND, interval.toInt())
			}
		}

		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, interval, pendingIntent)
	}

	private fun getInterval() = when(prefs.settings.checkForUpdates) {
		"0" -> AlarmManager.INTERVAL_DAY
		"1" -> AlarmManager.INTERVAL_DAY * 7
		"2" -> AlarmManager.INTERVAL_HOUR
		"3" -> AlarmManager.INTERVAL_HOUR * 12
		"4" -> AlarmManager.INTERVAL_HOUR * 6
		else -> Long.MAX_VALUE
	}

	private fun setHour(time: Calendar, hour: Int = prefs.settings.updateHour) = time.apply {
		set(Calendar.HOUR_OF_DAY, hour)
		set(Calendar.MINUTE, 0)
		set(Calendar.SECOND, 0)
		set(Calendar.MILLISECOND, 0)
	}

	private fun isEnabled() = prefs.settings.checkForUpdates != "5"

}