package com.durcheins.apkupdater.util

import com.durcheins.apkupdater.data.ui.AppInstallProgress
import com.durcheins.apkupdater.data.ui.AppInstallStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


class InstallLog {

    private val status = MutableSharedFlow<AppInstallStatus>(100)
    private val progress = MutableSharedFlow<AppInstallProgress>(100)
    @Volatile var currentInstallId: Int = 0

    fun status() = status.asSharedFlow()
    fun progress() = progress.asSharedFlow()

    fun cancelCurrentInstall() = status.tryEmit(AppInstallStatus(false, currentInstallId, false))
    fun emitStatus(newStatus: AppInstallStatus) = status.tryEmit(newStatus)
    fun emitProgress(newProgress: AppInstallProgress) = progress.tryEmit(newProgress)

}
