package com.apkupdater.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppInstallProgress
import com.apkupdater.prefs.Prefs
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@Suppress("SpellCheckingInspection")
class SessionInstaller(
    private val context: Context,
    private val installLog: InstallLog,
    private val prefs: Prefs
) {
    companion object {
        const val INSTALL_ACTION = "com.apkupdater.INSTALL_ACTION"
    }

    init {
        val installer = context.packageManager.packageInstaller
        installer.mySessions.forEach { try { installer.abandonSession(it.sessionId) } catch (_: Exception) {} }
    }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 26) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallSettings() {
        if (Build.VERSION.SDK_INT >= 26) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun finish() {
        val installer = context.packageManager.packageInstaller
        installer.mySessions.forEach { try { installer.abandonSession(it.sessionId) } catch (_: Exception) {} }
    }

    suspend fun installXapk(id: Int, packageName: String, stream: InputStream) {
        install(id, packageName, stream)
    }

    suspend fun install(id: Int, packageName: String, stream: InputStream) {
        val tempFile = File(context.cacheDir, "install_${id}_${System.currentTimeMillis()}.tmp")
        try {
            withContext(Dispatchers.IO) {
                copyWithProgress(id, stream, tempFile)
            }
            install(id, packageName, tempFile)
        } catch (e: Exception) {
            Log.e("SessionInstaller", "Install failed", e)
            installLog.emitStatus(AppInstallStatus(false, id, true, "Install Error: ${e.message}"))
        } finally {
            tempFile.delete()
        }
    }

    fun getContext() = context

    suspend fun install(id: Int, packageName: String, file: File) {
        if (isZipWithApks(file)) {
            installLog.log("Detected Bundle/XAPK for $packageName")
            installXapkFile(id, packageName, file)
        } else {
            install(id, packageName, listOf(file))
        }
    }

    private fun isZipWithApks(file: File): Boolean {
        return try {
            ZipFile(file).use { zip ->
                zip.entries().asSequence().any {
                    it.name.endsWith(".apk", ignoreCase = true) && !it.isDirectory
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun installXapkFile(id: Int, packageName: String, file: File) {
        installLog.log("Extracting Bundle/XAPK for $packageName")
        val tempDir = File(context.cacheDir, "xapk_ext_${id}_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val apks = mutableListOf<File>()
        try {
            withContext(Dispatchers.IO) {
                ZipFile(file).use { zip ->
                    zip.entries().asSequence()
                        .filter { it.name.endsWith(".apk", ignoreCase = true) && !it.isDirectory }
                        .forEach { entry ->
                            val name = entry.name.substringAfterLast("/")
                            val tempFile = File(tempDir, name)
                            zip.getInputStream(entry).use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            apks.add(tempFile)
                        }
                }
            }

            if (apks.isEmpty()) {
                throw Exception("No APKs found in bundle")
            }

            install(id, packageName, apks)

        } catch (e: Exception) {
            Log.e("SessionInstaller", "XAPK extraction failed", e)
            installLog.log("Bundle error for $packageName: ${e.message}")
            installLog.emitStatus(AppInstallStatus(false, id, true, "Bundle Error: ${e.message}"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    suspend fun install(id: Int, packageName: String, files: List<File>) {
        val rootEnabled = prefs.rootInstall.get() && withContext(Dispatchers.IO) { Shell.getShell().isRoot }
        val isSystem = isSystemApp(packageName)
        val isUpdate = isAppInstalled(packageName)

        // Logic: Use root if enabled AND (it's a system app OR it's a new installation).
        // For normal updates to user apps, use PackageInstaller (which supports silent updates on API 31+).
        if (rootEnabled && (isSystem || !isUpdate)) {
            runRootInstallFiles(id, packageName, files)
        } else {
            installLog.log("Using PackageInstaller for $packageName")
            val success = installNew(id, packageName, files)
            // Fallback to root if normal installation failed and root is available
            if (!success && rootEnabled) {
                installLog.log("PackageInstaller failed, trying root fallback for $packageName")
                runRootInstallFiles(id, packageName, files)
            }
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (_: Exception) {
            false
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("SessionInstaller", "App $packageName not installed: ${e.message}")
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun copyWithProgress(id: Int, input: InputStream, output: File) {
        val buffer = ByteArray(128 * 1024)
        var totalRead = 0L
        var lastEmitted = 0L
        input.use { inStream ->
            output.outputStream().use { outStream ->
                var bytes = inStream.read(buffer)
                while (bytes >= 0) {
                    outStream.write(buffer, 0, bytes)
                    totalRead += bytes
                    if (totalRead - lastEmitted > 512 * 1024) {
                        installLog.emitProgress(AppInstallProgress(id, totalRead))
                        lastEmitted = totalRead
                    }
                    bytes = inStream.read(buffer)
                }
                installLog.emitProgress(AppInstallProgress(id, totalRead))
                outStream.flush()
            }
        }
        if (totalRead == 0L) throw Exception("Empty stream")
    }

    private suspend fun runRootInstallFiles(id: Int, packageName: String, files: List<File>) = withContext(Dispatchers.IO) {
        val tmpFiles = mutableListOf<String>()
        try {
            files.forEach { file ->
                val tmpPath = "/data/local/tmp/${file.name}"
                Shell.cmd("rm -f '$tmpPath'").exec()
                Shell.cmd("cat '${file.absolutePath}' > '$tmpPath'").exec()
                Shell.cmd("chmod 666 '$tmpPath'").exec()
                tmpFiles.add(tmpPath)
            }

            val flags = "-r -d -g -t --user 0"
            val bypassFlag = if (Build.VERSION.SDK_INT >= 34) " --bypass-low-target-sdk-block" else ""

            installLog.log("Root: Installing $packageName (${files.size} files)")

            val res = if (files.size == 1) {
                Shell.cmd("pm install $flags$bypassFlag '${tmpFiles[0]}'").exec()
            } else {
                // Try pm install-multiple first
                var r = Shell.cmd("pm install-multiple $flags$bypassFlag ${tmpFiles.joinToString(" ") { "'$it'" }}").exec()
                if (!r.isSuccess && (r.out + r.err).any { it.contains("Unknown command", ignoreCase = true) }) {
                    // Fallback to session-based manual CLI install
                    val createRes = Shell.cmd("pm install-create $flags$bypassFlag").exec()
                    val sessionId = createRes.out.firstOrNull { it.contains("Success: created install session [") }
                        ?.substringAfter("[")?.substringBefore("]")

                    if (sessionId != null) {
                        var allWritesOk = true
                        tmpFiles.forEachIndexed { index, path ->
                            if (!Shell.cmd("pm install-write $sessionId split$index '$path'").exec().isSuccess) {
                                allWritesOk = false
                            }
                        }
                        r = if (allWritesOk) {
                            Shell.cmd("pm install-commit $sessionId").exec()
                        } else {
                            Shell.cmd("pm install-abandon $sessionId").exec()
                            createRes // Return original error context
                        }
                    }
                }
                r
            }

            if (!res.isSuccess) {
                val lastError = (res.out + res.err).joinToString("\n")
                installLog.log("Root Error: $packageName - $lastError")

                var friendlyError = lastError
                if (lastError.contains("REJECTED_BY_BUILDTYPE") || lastError.contains("-3001")) {
                    friendlyError = "Signature mismatch: Uninstall existing app first"
                } else if (lastError.contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
                    friendlyError = "Missing shared library (e.g. TrichromeLibrary)"
                } else if (lastError.contains("is a persistent app")) {
                    friendlyError = "Persistent system apps cannot be updated this way"
                }

                installLog.emitStatus(AppInstallStatus(false, id, true, "Root failed: $friendlyError"))
            } else {
                installLog.log("Root: $packageName success")
                installLog.emitStatus(AppInstallStatus(true, id, true))
            }
        } catch (e: Exception) {
            installLog.emitStatus(AppInstallStatus(false, id, true, "Root Crash: ${e.message}"))
        } finally {
            tmpFiles.forEach { Shell.cmd("rm -f '$it'").exec() }
        }
    }

    @SuppressLint("RequestInstallPackagesPolicy")
    private suspend fun installNew(
        id: Int,
        packageName: String,
        files: List<File>
    ): Boolean {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            params.setAppLabel(appInfo.loadLabel(context.packageManager))
        } catch (_: Exception) {
            params.setAppLabel(packageName)
        }

        if (Build.VERSION.SDK_INT >= 24) params.setAppPackageName(packageName)
        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= 33) params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)

        val totalSize = files.sumOf { it.length() }
        params.setSize(totalSize)

        return suspendCancellableCoroutine { continuation ->
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("UnsafeIntentLaunch")
                override fun onReceive(context: Context, intent: Intent?) {
                    val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
                    val message = intent?.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                    if (status == -1) return

                    when (status) {
                        PackageInstaller.STATUS_SUCCESS -> {
                            installLog.log("Success: $packageName")
                            installLog.emitStatus(AppInstallStatus(true, id, true))
                            try { context.unregisterReceiver(this) } catch (_: Exception) {}
                            if (continuation.isActive) continuation.resume(true) {_, _, _ -> }
                        }
                        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                            val confirmIntent = if (Build.VERSION.SDK_INT >= 33) {
                                intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                intent.getParcelableExtra(Intent.EXTRA_INTENT)
                            }

                            confirmIntent?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                            if (confirmIntent != null) {
                                context.startActivity(confirmIntent)
                            } else {
                                installLog.emitStatus(AppInstallStatus(false, id, true, "User action required but intent missing"))
                                try { context.unregisterReceiver(this) } catch (_: Exception) {}
                                if (continuation.isActive) continuation.resume(false) {_, _, _ -> }
                            }
                        }
                        else -> {
                            installLog.log("Error $status: $packageName - $message")
                            var friendlyError = message ?: "Failed ($status)"
                            if (friendlyError.contains("REJECTED_BY_BUILDTYPE") || status == -3001) {
                                friendlyError = "Signature mismatch: Uninstall existing app first"
                            }
                            installLog.emitStatus(AppInstallStatus(false, id, true, friendlyError))
                            try { context.unregisterReceiver(this) } catch (_: Exception) {}
                            if (continuation.isActive) continuation.resume(false) {_, _, _ -> }
                        }
                    }
                }
            }

            val timestamp = System.currentTimeMillis()
            val actionId = "$INSTALL_ACTION.$id.$timestamp"
            val filter = IntentFilter(actionId)
            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }

            try {
                val sessionId = packageInstaller.createSession(params)
                continuation.invokeOnCancellation {
                    try { packageInstaller.abandonSession(sessionId) } catch (_: Exception) {}
                }
                packageInstaller.openSession(sessionId).use { session ->
                    var baseFound = false
                    files.forEachIndexed { index, file ->
                        val info = context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
                        val isBase = info != null && !baseFound && (info.packageName == packageName || files.size == 1)
                        val name = if (isBase) {
                            baseFound = true
                            "base.apk"
                        } else {
                            "split_$index.apk"
                        }

                        file.inputStream().use { input ->
                            session.openWrite(name, 0, file.length()).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    val broadcastIntent = Intent(actionId).apply { setPackage(context.packageName) }
                    val pending = PendingIntent.getBroadcast(context, sessionId, broadcastIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
                    session.commit(pending.intentSender)
                }
            } catch (e: Exception) {
                Log.e("SessionInstaller", "Session failed", e)
                try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
                installLog.emitStatus(AppInstallStatus(false, id, true, "Session failure: ${e.message}"))
                if (continuation.isActive) continuation.resume(false) {_, _, _ -> }
            }
        }
    }
}
