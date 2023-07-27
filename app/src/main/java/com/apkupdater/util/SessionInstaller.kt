package com.apkupdater.util

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import com.apkupdater.ui.activity.MainActivity
import java.io.File


class SessionInstaller(private val context: Context) {

    companion object {
        const val InstallAction = "installAction"
    }

    fun install(packageName: String, file: File) {
        val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)
        params.setOriginatingUid(android.os.Process.myUid())

        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }

        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        session.openWrite(file.name, 0, file.length()).use { output ->
            file.inputStream().copyTo(output)
            session.fsync(output)
        }

        val intent = Intent(context, MainActivity::class.java).apply { action = InstallAction }
        val pending = PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
        session.commit(pending.intentSender)
        session.close()
    }

}
