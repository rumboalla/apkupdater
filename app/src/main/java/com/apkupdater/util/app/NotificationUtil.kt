package com.apkupdater.util.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apkupdater.R
import com.apkupdater.activity.MainActivity

class NotificationUtil(private val context: Context) {

	private val notificationManager get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	private val channelId = context.getString(R.string.notification_channel_id)
	private val channelName = context.getString(R.string.notification_channel_name)
	private val updateTitle = context.getString(R.string.notification_update_title)
	private val updateAction = context.getString(R.string.notification_update_action)
	private val updateId = 28132

	fun showUpdateNotification(num: Int) {
		// Intent for the notification click
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = FLAG_ACTIVITY_REORDER_TO_FRONT
			action = updateAction
		}

		val builder = NotificationCompat.Builder(context, channelId)
			.setSmallIcon(R.drawable.ic_update_black_24dp)
			.setContentTitle(updateTitle)
			.setContentText(context.resources.getQuantityString(R.plurals.notification_update_description, num, num))
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
			.setAutoCancel(true)

		createNotificationChannel()
		NotificationManagerCompat.from(context).notify(updateId, builder.build())
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
				description = context.getString(R.string.notification_channel_description)
			}
			notificationManager.createNotificationChannel(channel)
		}
	}
}