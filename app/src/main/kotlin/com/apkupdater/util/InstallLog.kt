package com.apkupdater.util

import com.apkupdater.data.ui.AppInstallProgress
import com.apkupdater.data.ui.AppInstallStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow


class InstallLog {

    private val status = MutableSharedFlow<AppInstallStatus>(100)
    private val progress = MutableSharedFlow<AppInstallProgress>(100)
    private val statusUpdate = MutableSharedFlow<Pair<Int, String>>(100)
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    
    @Volatile var currentInstallId: Int = 0

    fun status() = status.asSharedFlow()
    fun progress() = progress.asSharedFlow()
    fun statusUpdate() = statusUpdate.asSharedFlow()
    fun logs() = _logs.asStateFlow()

    fun cancelCurrentInstall() = status.tryEmit(AppInstallStatus(false, currentInstallId, false))
    
    fun emitStatus(newStatus: AppInstallStatus) {
        status.tryEmit(newStatus)
        if (newStatus.success) {
            log("Install success for ID: ${newStatus.id}")
        } else {
            log("Install failed for ID: ${newStatus.id}. Error: ${newStatus.errorMessage ?: "Unknown error"}")
        }
    }
    
    fun emitProgress(newProgress: AppInstallProgress) = progress.tryEmit(newProgress)

    fun emitStatusUpdate(id: Int, status: String) {
        statusUpdate.tryEmit(id to status)
    }
    
    fun log(event: String) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(event)
        if (currentLogs.size > 200) {
            currentLogs.removeAt(0)
        }
        _logs.value = currentLogs
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
