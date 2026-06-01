package com.apkupdater.viewmodel

import android.util.Log
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.R
import com.apkupdater.data.snack.TextSnack
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppInstallProgress
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Link
import com.apkupdater.prefs.Prefs
import com.apkupdater.util.Downloader
import com.apkupdater.util.InstallLog
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.SnackBar
import com.apkupdater.util.Stringer
import com.apkupdater.util.UpdatesNotification
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap


abstract class InstallViewModel(
    protected val downloader: Downloader,
    protected val installer: SessionInstaller,
    protected val prefs: Prefs,
    protected val snackBar: SnackBar,
    protected val stringer: Stringer,
    protected val installLog: InstallLog,
    protected val notification: UpdatesNotification,
): ViewModel() {

    private var lastProgressUpdate = 0L
    private val maxProgressMap = mutableMapOf<Int, Int>()
    protected val installJobs = ConcurrentHashMap<Int, Job>()

    fun install(update: AppUpdate, uriHandler: UriHandler) {
        installLog.log("Update clicked for ${update.name}")
        
        if (update.isPersistent) {
            installLog.log("Cannot update persistent app: ${update.packageName}")
            snackBar.snackBar(viewModelScope, TextSnack(stringer.get(R.string.persistent_app_warning)))
            return
        }

        notification.showStatus(update.name, stringer.get(R.string.notif_checking))
        updateAppStatus(update.id, stringer.get(R.string.notif_checking))
        
        when (update.source) {
            ApkMirrorSource -> {
                installLog.log("Opening ApkMirror link for ${update.name}")
                uriHandler.openUri((update.link as Link.Url).link)
            }
            else -> {
                val job = if (prefs.rootInstall.get()) {
                    installLog.log("Starting root install for ${update.name}")
                    downloadAndRootInstall(update)
                } else {
                    installLog.log("Starting standard install for ${update.name}")
                    downloadAndInstall(update)
                }
                installJobs[update.id] = job
                job.invokeOnCompletion { installJobs.remove(update.id) }
            }
        }
    }

    protected fun subscribeToInstallStatus() = installLog.status().onEach {
        sendInstallSnack(it)
        val appName = "App"
        if (it.success) {
            installLog.log("Install success for ID: ${it.id}")
            notification.showStatus(appName, stringer.get(R.string.notif_success), success = true)
            maxProgressMap.remove(it.id)
            finishInstall(it.id).join()
        } else {
            installLog.log("Install failed for ID: ${it.id}. Error: ${it.errorMessage}")
            notification.showStatus(appName, stringer.get(R.string.notif_failed), false)
            maxProgressMap.remove(it.id)
            installLog.emitProgress(AppInstallProgress(it.id, 0L))
            cancelInstall(it.id).join()
        }
    }.launchIn(viewModelScope)

    protected fun subscribeToInstallProgress(
        block: (AppInstallProgress) -> Unit
    ) = installLog.progress().onEach { progressEvent ->
        val id = progressEvent.id
        val total = progressEvent.total ?: 0L
        val current = progressEvent.progress ?: 0L
        
        var progressPercent = if (total > 0) {
            ((current.toFloat() / total.toFloat()) * 100).toInt()
        } else {
            0
        }
        
        val lastMax = maxProgressMap[id] ?: 0
        if (progressPercent < lastMax) {
            progressPercent = lastMax
        } else {
            maxProgressMap[id] = progressPercent
        }
        
        block(progressEvent.copy(progress = current))

        val now = System.currentTimeMillis()
        if ((now - lastProgressUpdate) > 500) {
            lastProgressUpdate = now
            notification.updateProgress("Updating...", stringer.get(R.string.notif_downloading), progressPercent, total <= 0L)
        }
    }.launchIn(viewModelScope)

    protected suspend fun downloadAndInstall(id: Int, packageName: String, link: Link) = runCatching {
        installLog.emitProgress(AppInstallProgress(id, 0L))
        installLog.log("Download started for $packageName")
        notification.showStatus(packageName, stringer.get(R.string.notif_downloading))
        updateAppStatus(id, stringer.get(R.string.notif_downloading))
        
        when (link) {
            Link.Empty -> { 
                Log.e("InstallViewModel", "downloadAndInstall: Unsupported.")
                installLog.log("Error: Unsupported link for $packageName")
            }
            is Link.Play -> {
                val (files, headers) = link.getInstallFiles()
                val totalSize = files.sumOf { it.size }
                installLog.emitProgress(AppInstallProgress(id, 0L, totalSize))
                
                val tempFiles = mutableListOf<File>()
                var currentProgress = 0L
                try {
                    files.forEachIndexed { index, file ->
                        val response = downloader.downloadResponse(file.url, headers)
                            ?: throw Exception("Failed to download split $index")
                        
                        val tempFile = File(installer.getContext().cacheDir, "${packageName}_${index}_${System.currentTimeMillis()}.apk")
                        tempFiles.add(tempFile)
                        
                        val buffer = ByteArray(128 * 1024)
                        response.body.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                var bytes = input.read(buffer)
                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    currentProgress += bytes
                                    installLog.emitProgress(AppInstallProgress(id, currentProgress))
                                    bytes = input.read(buffer)
                                }
                                output.flush()
                            }
                        }
                    }
                    installer.install(id, packageName, tempFiles)
                } finally {
                    tempFiles.forEach { it.delete() }
                }
            }
            is Link.Url -> {
                val response = downloader.downloadResponse(link.link)
                val totalSize = response?.body?.contentLength() ?: 0L
                installLog.emitProgress(AppInstallProgress(id, 0L, totalSize))
                
                val isXapk = link.link.contains(".xapk", ignoreCase = true)
                response?.body?.byteStream()?.let { stream ->
                    if (isXapk) {
                        installer.installXapk(id, packageName, stream)
                    } else {
                        installer.install(id, packageName, stream)
                    }
                }
            }
            is Link.Xapk -> {
                val response = downloader.downloadResponse(link.link)
                val totalSize = response?.body?.contentLength() ?: 0L
                installLog.emitProgress(AppInstallProgress(id, 0L, totalSize))
                response?.body?.byteStream()?.let { stream ->
                    installer.installXapk(id, packageName, stream)
                }
            }
        }
        
        val isRoot = prefs.rootInstall.get()
        val nextStatus = if (isRoot) R.string.notif_installing else R.string.notif_confirm
        
        installLog.log("${stringer.get(nextStatus)} $packageName")
        notification.showStatus(packageName, stringer.get(nextStatus))
        updateAppStatus(id, stringer.get(nextStatus))
    }.getOrElse {
        Log.e("InstallViewModel", "Error in downloadAndInstall.", it)
        installLog.log("Download error for $packageName: ${it.message}")
        notification.showStatus(packageName, stringer.get(R.string.notif_failed), false)
        cancelInstall(id)
    }

    open fun cancelInstall(id: Int): Job {
        installJobs[id]?.cancel()
        return viewModelScope.launch {
            // Placeholder, actual implementation in subclasses will update UI
        }
    }

    protected abstract fun sendInstallSnack(log: AppInstallStatus)
    protected abstract fun downloadAndInstall(update: AppUpdate): Job
    protected abstract fun downloadAndRootInstall(update: AppUpdate): Job
    protected abstract fun finishInstall(id: Int): Job
    protected abstract fun updateAppStatus(id: Int, status: String)
}
