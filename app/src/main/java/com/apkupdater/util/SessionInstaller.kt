package com.apkupdater.util

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import androidx.core.content.ContextCompat.startActivity
import com.apkupdater.BuildConfig
import com.apkupdater.ui.activity.MainActivity
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean


class SessionInstaller(private val context: Context) {

    companion object {
        const val InstallAction = "installAction"
    }

    private val installMutex = AtomicBoolean(false)

    suspend fun install(id: Int, packageName: String, stream: InputStream) {
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

        session.openWrite(randomUUID(), 0, -1).use { output ->
            stream.copyTo(output)
            stream.close()
            session.fsync(output)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "$InstallAction.$id"
        }

        installMutex.lock()
        val pending = PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
        session.commit(pending.intentSender)
        session.close()
    }

    fun rootInstall(file: File): Boolean {
        val res = Shell.Pool.SU.run("pm install -r ${file.absolutePath}") == 0
        file.delete()
        return res
    }

    fun finish() = installMutex.unlock()

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!context.packageManager.canRequestPackageInstalls()) {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                val intent = Intent(ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(context, intent, null)
                return false
            }
        }
        return true
    }

}
