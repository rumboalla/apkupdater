package com.apkupdater.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apkupdater.R
import com.apkupdater.ui.activity.MainActivity

class UpdatesNotification(private val context: Context) {

    companion object {
        const val UpdateAction = "updateAction"
        private const val UPDATE_ID = 42
        private const val BACKGROUND_ID = 43
    }

    private val notificationManager get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = context.getString(R.string.notification_channel_id)
    private val channelName = context.getString(R.string.notification_channel_name)

    private fun getBaseBuilder(): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            action = UpdateAction
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_install)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    @SuppressLint("MissingPermission")
    fun updateProgress(title: String, text: String, progress: Int, indeterminate: Boolean = false) {
        if (!areNotificationsEnabled()) return
        createNotificationChannel()

        val builder = getBaseBuilder()
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, progress, indeterminate)
            .setOngoing(true)

        NotificationManagerCompat.from(context).notify(UPDATE_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showStatus(title: String, text: String, success: Boolean? = null) {
        if (!areNotificationsEnabled()) return
        createNotificationChannel()

        val builder = getBaseBuilder()
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(UPDATE_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showUpdateNotification(count: Int) {
        if (!areNotificationsEnabled()) return
        createNotificationChannel()

        val title = context.getString(R.string.notification_update_title)
        val description = context.resources.getQuantityString(R.plurals.notification_update_description, count, count)

        val builder = getBaseBuilder()
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(BACKGROUND_ID, builder.build())
    }

    fun checkNotificationPermission(launcher: ManagedActivityResultLauncher<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (!areNotificationsEnabled()) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun areNotificationsEnabled() = NotificationManagerCompat
        .from(context)
        .areNotificationsEnabled()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

}
