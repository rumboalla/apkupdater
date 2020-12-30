package com.apkupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.app.NotificationUtil
import com.kryptoprefs.invoke
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

	private val updatesRepository: UpdatesRepository by inject()
	private val notificationUtil: NotificationUtil by inject()
	private val prefs: AppPrefs by inject()

	override fun onReceive(context: Context, intent: Intent?): Unit = runBlocking {
		updatesRepository.getUpdatesAsync().await().fold(
			onSuccess = {
				prefs.updates(it)
				notificationUtil.showUpdateNotification(it.size)
			},
			onFailure = { Log.e("AlarmReceiver", "onReceive", it) }
		)
	}

}