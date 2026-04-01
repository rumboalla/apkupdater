package com.durcheins.apkupdater.viewmodel

import android.util.Log
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.snack.TextSnack
import com.durcheins.apkupdater.data.ui.ApkMirrorSource
import com.durcheins.apkupdater.data.ui.AppInstallProgress
import com.durcheins.apkupdater.data.ui.AppInstallStatus
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.data.ui.Link
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.util.Downloader
import com.durcheins.apkupdater.util.InstallLog
import com.durcheins.apkupdater.util.SessionInstaller
import com.durcheins.apkupdater.util.SnackBar
import com.durcheins.apkupdater.util.Stringer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


abstract class InstallViewModel(
    private val downloader: Downloader,
    private val installer: SessionInstaller,
    private val prefs: Prefs,
    private val snackBar: SnackBar,
    private val stringer: Stringer,
    private val installLog: InstallLog
): ViewModel() {

    fun install(update: AppUpdate, uriHandler: UriHandler) {
        when (update.source) {
            ApkMirrorSource -> uriHandler.openUri((update.link as Link.Url).link)
            else -> downloadAndInstall(update)
        }
    }

    protected fun subscribeToInstallStatus() = installLog.status().onEach {
        sendInstallSnack(it)
        if (it.success) {
            finishInstall(it.id).join()
        } else {
            installLog.emitProgress(AppInstallProgress(it.id, 0L))
            cancelInstall(it.id).join()
        }
    }.launchIn(viewModelScope)

    protected fun subscribeToInstallProgress(
        block: (AppInstallProgress) -> Unit
    ) = installLog.progress().onEach {
        block(it)
    }.launchIn(viewModelScope)

    protected suspend fun downloadAndInstall(id: Int, packageName: String, link: Link) = runCatching {
        installLog.emitProgress(AppInstallProgress(id, 0L))
        when (link) {
            Link.Empty -> { Log.e("InstallViewModel", "downloadAndInstall: Unsupported.")}
            is Link.Play -> {
                val files = link.getInstallFiles()
                installLog.emitProgress(AppInstallProgress(id, 0L, files.sumOf { it.size }))
                installer.install(id, packageName, files.map { downloader.downloadStream(it.url)!! })
            }
            is Link.Url -> {
                installLog.emitProgress(AppInstallProgress(id, 0L, link.size))
                installer.install(id, packageName, downloader.downloadStream(link.link)!!)
            }
            is Link.Xapk -> installer.installXapk(id, packageName, downloader.downloadStream(link.link)!!)
        }
    }.getOrElse {
        Log.e("InstallViewModel", "Error in downloadAndInstall.", it)
        cancelInstall(id)
    }

    protected abstract fun sendInstallSnack(log: AppInstallStatus)
    protected abstract fun downloadAndInstall(update: AppUpdate): Job
    protected abstract fun cancelInstall(id: Int): Job
    protected abstract fun finishInstall(id: Int): Job
}
