package com.apkupdater.util

import com.apkupdater.data.ui.AppInstallProgress
import com.apkupdater.data.ui.AppInstallStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


class InstallLog {

    private val status = MutableSharedFlow<AppInstallStatus>(100)
    private val progress = MutableSharedFlow<AppInstallProgress>(100)
    private var currentInstallLog: Int = 0

    fun status() = status.asSharedFlow()
    fun progress() = progress.asSharedFlow()

    fun cancelCurrentInstall() = status.tryEmit(AppInstallStatus(false, currentInstallLog, false))
    fun emitStatus(newStatus: AppInstallStatus) = status.tryEmit(newStatus)
    fun emitProgress(newProgress: AppInstallProgress) = progress.tryEmit(newProgress)

}
