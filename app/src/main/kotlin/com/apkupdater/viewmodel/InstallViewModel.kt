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
            else -> {
                if (prefs.rootInstall.get()) {
                    downloadAndRootInstall(update)
                } else {
                    downloadAndInstall(update)
                }
            }
        }
    }

    protected fun subscribeToInstallStatus(
        block: (AppInstallStatus) -> Unit
    ) = installLog.status().onEach {
        block(it)
        if (it.success) {
            finishInstall(it.id).join()
        } else {
            cancelInstall(it.id).join()
        }
    }.launchIn(viewModelScope)

    protected fun subscribeToInstallProgress(
        block: (AppInstallProgress) -> Unit
    ) = installLog.progress().onEach {
        block(it)
    }.launchIn(viewModelScope)

    protected fun downloadAndRootInstall(id: Int, link: Link) = runCatching {
        when (link) {
            is Link.Url -> {
                if (installer.rootInstall(downloader.download(link.link))) {
                    finishInstall(id)
                } else {
                    cancelInstall(id)
                }
            }
            else -> snackBar.snackBar(
                viewModelScope,
                TextSnack(stringer.get(R.string.root_install_not_supported))
            )
        }
    }.getOrElse {
        Log.e("InstallViewModel", "Error in downloadAndRootInstall.", it)
        cancelInstall(id)
    }

    protected suspend fun downloadAndInstall(id: Int, packageName: String, link: Link) = runCatching {
        when (link) {
            Link.Empty -> { Log.e("InstallViewModel", "downloadAndInstall: Unsupported.")}
            is Link.Play -> {
                val files = link.getInstallFiles()
                installLog.emitProgress(AppInstallProgress(id, 0L, files.sumOf { it.size }))
                installer.playInstall(id, packageName, files.map { downloader.downloadStream(it.url)!! })
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

    protected fun sendInstallSnack(updates: List<AppUpdate>, log: AppInstallStatus) {
        if (log.snack) {
            updates.find { log.id == it.id }?.let { app ->
                val message = if (log.success) R.string.install_success else R.string.install_failure
                snackBar.snackBar(viewModelScope, TextSnack(stringer.get(message, app.name)))
            }
        }
    }

    protected abstract fun downloadAndInstall(update: AppUpdate): Job
    protected abstract fun downloadAndRootInstall(update: AppUpdate): Job
    protected abstract fun cancelInstall(id: Int): Job
    protected abstract fun finishInstall(id: Int): Job
}
