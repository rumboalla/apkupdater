package com.apkupdater.viewmodel

import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.snack.InstallSnack
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
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
            ApkMirrorSource -> uriHandler.openUri(update.link)
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

    protected fun downloadAndRootInstall(id: Int, link: String) {
        val file = downloader.download(link)
        val res = installer.rootInstall(file)
        if (res) {
            finishInstall(id)
        } else {
            cancelInstall(id)
        }
    }

    protected suspend fun downloadAndInstall(id: Int, packageName: String, link: String) {
        val stream = downloader.downloadStream(link)
        if (stream != null) {
            if (link.contains("/XAPK")) {
                installer.installXapk(id, packageName, stream)
            } else {
                installer.install(id, packageName, stream)
            }
        } else {
            cancelInstall(id)
        }
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
