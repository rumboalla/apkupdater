package com.durcheins.apkupdater.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import android.util.Log
import androidx.core.net.toUri
import com.durcheins.apkupdater.BuildConfig
import com.durcheins.apkupdater.data.ui.AppInstallProgress
import com.durcheins.apkupdater.data.ui.AppInstallStatus
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.io.DEFAULT_BUFFER_SIZE
import kotlin.io.copyTo
import kotlin.io.outputStream
import kotlin.use


@OptIn(ExperimentalAtomicApi::class)
class SessionInstaller(
    private val context: Context,
    private val installLog: InstallLog,
    private val prefs: Prefs
) {
    companion object {
        const val INSTALL_ACTION = "installAction"
    }

    init {
        // Cleanup old sessions
        val installer = context.packageManager.packageInstaller
        installer.mySessions.forEach { installer.abandonSession(it.sessionId) }
    }

    private val installMutex = AtomicBoolean(false)
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun install(id: Int, packageName: String, stream: InputStream) {
        if (prefs.newInstaller.get()) installNew(id, packageName, listOf(stream)) else installOld(id, packageName, listOf(stream))
    }

    suspend fun install(id: Int, packageName: String, streams: List<InputStream>) {
        if (prefs.newInstaller.get()) installNew(id, packageName, streams) else installOld(id, packageName, streams)
    }

    @SuppressLint("RequestInstallPackagesPolicy")
    private suspend fun installNew(
        id: Int,
        packageName: String,
        streams: List<InputStream>
    ): Boolean {
        val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (Build.VERSION.SDK_INT >= 24) params.setAppPackageName(packageName)
        if (Build.VERSION.SDK_INT >= 31) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        if (Build.VERSION.SDK_INT >= 33) params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)


        return suspendCancellableCoroutine { continuation ->

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, @SuppressLint("UnsafeIntentLaunch") intent: Intent?) {
                    Log.i("InstallerReceiver", "onReceive called with intent: $intent")
                    when (val extra = intent?.extras?.getInt(PackageInstaller.EXTRA_STATUS)) {
                        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                            Log.i("InstallerReceiver", "STATUS_PENDING_USER_ACTION")
                            installLog.currentInstallId = intent.getAppId() ?: 0
                            // Launch intent to confirm install
                            intent.getIntentExtra()?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                scope.launch {
                                    mutex.lock()
                                    context.startActivity(it)
                                }
                            }
                        }
                        PackageInstaller.STATUS_SUCCESS -> {
                            Log.i("InstallerReceiver", "SUCCESS: $extra")
                            installLog.emitStatus(AppInstallStatus(true, id, true))
                            context.unregisterReceiver(this)
                            if (mutex.isLocked) mutex.unlock()
                            if (continuation.isActive) continuation.resume(true) {_, _, _ -> }
                        }
                        else -> {
                            Log.i("InstallerReceiver", "FAILURE: $extra")
                            installLog.emitStatus(AppInstallStatus(false, id, true))
                            context.unregisterReceiver(this)
                            if (mutex.isLocked) mutex.unlock()
                            if (continuation.isActive) continuation.resume(false) {_, _, _ -> }
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(receiver, IntentFilter("$INSTALL_ACTION.$id"), Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, IntentFilter("$INSTALL_ACTION.$id"))
            }

            val callback = object : PackageInstaller.SessionCallback() {
                override fun onBadgingChanged(sessionId: Int) {}
                override fun onActiveChanged(sessionId: Int, active: Boolean) {}
                override fun onCreated(sessionId: Int) {}
                override fun onProgressChanged(sessionId: Int, p: Float) {}
                override fun onFinished(sessionId: Int, success: Boolean) {
                    Log.i("InstallerCallback", "onFinished($sessionId) called with success: $success")
                    packageInstaller.unregisterSessionCallback(this)
                    runCatching { packageInstaller.openSession(sessionId).close() }.getOrNull()
                }
            }

            packageInstaller.registerSessionCallback(callback, Handler(Looper.getMainLooper()))

            continuation.invokeOnCancellation {
                runCatching {
                    context.unregisterReceiver(receiver)
                    packageInstaller.unregisterSessionCallback(callback)
                }.getOrNull()
            }

            val sessionId = packageInstaller.createSession(params)
            var totalBytes = 0L
            packageInstaller.openSession(sessionId).use { session ->
                streams.forEach { stream ->
                    session.openWrite("$packageName.${randomUUID()}", 0, -1).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = stream.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            totalBytes += bytes
                            installLog.emitProgress(AppInstallProgress(id, totalBytes))
                            bytes = stream.read(buffer)
                        }
                        session.fsync(output)
                    }
                    stream.close()
                }

                val intent = Intent("$INSTALL_ACTION.$id").apply { setPackage(context.packageName) }
                val pending = PendingIntent.getBroadcast(context, sessionId, intent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
                session.commit(pending.intentSender)
            }
        }
    }

    @SuppressLint("RequestInstallPackagesPolicy")
    private suspend fun installOld(id: Int, packageName: String, streams: List<InputStream>) {
        val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)

        if (Build.VERSION.SDK_INT > 24) {
            params.setOriginatingUid(Process.myUid())
        }

        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }

        val sessionId = packageInstaller.createSession(params)
        var bytes = 0L
        packageInstaller.openSession(sessionId).use { session ->
            streams.forEach {
                session.openWrite("$packageName.${randomUUID()}", 0, -1).use { output ->
                    bytes += it.copyToAndNotify(output, id, installLog, bytes)
                    it.close()
                    session.fsync(output)
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = "$INSTALL_ACTION.$id"
            }

            installMutex.lock()
            val pending = PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
            session.commit(pending.intentSender)
            session.close()
        }
    }

    fun finish() = installMutex.unlock()

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!context.packageManager.canRequestPackageInstalls()) {
                val uri = "package:${BuildConfig.APPLICATION_ID}".toUri()
                val intent = Intent(ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent, null)
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

fun InputStream.copyToAndNotify(out: OutputStream, id: Int, installLog: InstallLog, total: Long, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        installLog.emitProgress(AppInstallProgress(id, progress = total + bytesCopied))
        bytes = read(buffer)
    }
    return bytesCopied
}
