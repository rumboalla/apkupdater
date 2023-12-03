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
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile


class SessionInstaller(private val context: Context) {

    companion object {
        const val InstallAction = "installAction"
    }

    private val installMutex = AtomicBoolean(false)

    suspend fun install(id: Int, packageName: String, stream: InputStream) =
        install(id, packageName, listOf(stream))

    private suspend fun install(id: Int, packageName: String, streams: List<InputStream>) {
        val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)

        if (Build.VERSION.SDK_INT > 24) {
            params.setOriginatingUid(android.os.Process.myUid())
        }

        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }

        val sessionId = packageInstaller.createSession(params)
        packageInstaller.openSession(sessionId).use { session ->
            streams.forEach {
                session.openWrite("$packageName.${randomUUID()}", 0, -1).use { output ->
                    it.copyTo(output)
                    it.close()
                    session.fsync(output)
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = "$InstallAction.$id"
            }

            installMutex.lock()
            val pending = PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
            session.commit(pending.intentSender)
            session.close()
        }
    }

    fun rootInstall(file: File): Boolean {
        val res = Shell.cmd("pm install -r ${file.absolutePath}").exec().isSuccess
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

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun installXapk(id: Int, packageName: String, stream: InputStream) {
        // Copy file to disk.
        // TODO: Find a way to do this without saving file
        val file = File(context.cacheDir, randomUUID())
        stream.copyTo(file.outputStream())

        // Get entries
        val zip = ZipFile(file)
        val entries = zip.entries().toList()

        // Install all the apks
        // TODO: Try to install only needed apks
        // TODO: Add root install support
        val apks = entries.filter { it.name.contains(".apk") }.map { zip.getInputStream(it) }
        install(id, packageName, apks)

        // Cleanup
        zip.close()
        file.delete()
    }

}
