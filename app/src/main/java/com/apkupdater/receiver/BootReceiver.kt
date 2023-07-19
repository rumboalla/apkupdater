package com.apkupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apkupdater.worker.UpdatesWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.e("BootReceiver", "Test")
            UpdatesWorker.launch(context)
        }
    }

}
