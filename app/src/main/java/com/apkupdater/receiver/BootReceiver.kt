package com.apkupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apkupdater.util.app.AlarmUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

	private val alarmUtil: AlarmUtil by inject()

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == "android.intent.action.BOOT_COMPLETED") {
			alarmUtil.setupAlarm(context)
		}
	}

}
