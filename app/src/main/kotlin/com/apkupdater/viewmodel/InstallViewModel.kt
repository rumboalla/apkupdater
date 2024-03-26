package com.apkupdater.viewmodel

import android.util.Log
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.R
import com.apkupdater.data.snack.InstallSnack
import com.apkupdater.data.snack.TextIdSnack
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Link
import com.apkupdater.prefs.Prefs
import com.apkupdater.util.Downloader
import com.apkupdater.util.SessionInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


abstract class InstallViewModel(
    private val mainViewModel: MainViewModel,
    private val downloader: Downloader,
    private val installer: SessionInstaller,
    private val prefs: Prefs
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

    protected fun subscribeToInstallLog(
        block: (AppInstallStatus) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        mainViewModel.appInstallLog.collect {
            block(it)
            if (it.success) {
                finishInstall(it.id).join()
            } else {
                cancelInstall(it.id).join()
            }
        }
    }

    protected fun downloadAndRootInstall(id: Int, link: Link) = runCatching {
        when (link) {
            is Link.Url -> {
                if (installer.rootInstall(downloader.download(link.link))) {
                    finishInstall(id)
                } else {
                    cancelInstall(id)
                }
            }
            else -> { mainViewModel.sendSnack(TextIdSnack(R.string.root_install_not_supported))}
        }

    }.getOrElse {
        Log.e("InstallViewModel", "Error in downloadAndRootInstall.", it)
        cancelInstall(id)
    }

    protected suspend fun downloadAndInstall(id: Int, packageName: String, link: Link) = runCatching {
        when (link) {
            Link.Empty -> { Log.e("InstallViewModel", "downloadAndInstall: Unsupported.")}
            is Link.Play -> installer.playInstall(id, packageName, link.getInstallFiles().map { downloader.downloadStream(it)!! })
            is Link.Url -> installer.install(id, packageName, downloader.downloadStream(link.link)!!)
            is Link.Xapk -> installer.installXapk(id, packageName, downloader.downloadStream(link.link)!!)
        }
    }.getOrElse {
        Log.e("InstallViewModel", "Error in downloadAndInstall.", it)
        cancelInstall(id)
    }

    protected fun sendInstallSnack(updates: List<AppUpdate>, log: AppInstallStatus) {
        if (log.snack) {
            updates.find { log.id == it.id }?.let { app ->
                mainViewModel.sendSnack(InstallSnack(log.success, app.name))
            }
        }
    }

    protected abstract fun downloadAndInstall(update: AppUpdate): Job
    protected abstract fun downloadAndRootInstall(update: AppUpdate): Job
    protected abstract fun cancelInstall(id: Int): Job
    protected abstract fun finishInstall(id: Int): Job
}
