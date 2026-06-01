package com.apkupdater.viewmodel

import androidx.lifecycle.viewModelScope
import com.apkupdater.R
import com.apkupdater.data.snack.TextSnack
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.data.ui.removeId
import com.apkupdater.data.ui.setError
import com.apkupdater.data.ui.setIsInstalling
import com.apkupdater.data.ui.setProgress
import com.apkupdater.data.ui.setStatus
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.SearchRepository
import com.apkupdater.util.Downloader
import com.apkupdater.util.InstallLog
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.SnackBar
import com.apkupdater.util.Stringer
import com.apkupdater.util.UpdatesNotification
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


class SearchViewModel(
    private val repository: SearchRepository,
    downloader: Downloader,
    installer: SessionInstaller,
    prefs: Prefs,
    snackBar: SnackBar,
    stringer: Stringer,
    installLog: InstallLog,
    notification: UpdatesNotification,
) : InstallViewModel(downloader, installer, prefs, snackBar, stringer, installLog, notification) {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))

    init {
        subscribeToInstallStatus()
        subscribeToInstallProgress { progress ->
            (state.value as? SearchUiState.Success)?.let { currentState ->
                state.value = SearchUiState.Success(currentState.updates.toMutableList().setProgress(progress))
            }
        }
    }

    fun state(): StateFlow<SearchUiState> = state

    fun search(query: String) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        if (query.isEmpty()) return@launchWithMutex
        state.value = SearchUiState.Loading
        repository.search(query).collect { result ->
            result.onSuccess {
                state.value = SearchUiState.Success(it)
            }.onFailure {
                state.value = SearchUiState.Error(it.message ?: stringer.get(R.string.something_went_wrong))
            }
        }
    }

    override fun cancelInstall(id: Int): Job = viewModelScope.launch(Dispatchers.IO) {
        super.cancelInstall(id).join()
        val currentState = state.value
        if (currentState is SearchUiState.Success) {
            state.value = SearchUiState.Success(currentState.updates.toMutableList().setIsInstalling(id = id, b = false))
        }
        installer.finish()
    }

    override fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Success(state.value.mutableUpdates().removeId(id))
        installer.finish()
    }

    override fun downloadAndRootInstall(update: AppUpdate): Job = downloadAndInstall(update)

    override fun downloadAndInstall(update: AppUpdate): Job = viewModelScope.launch(Dispatchers.IO) {
        if(installer.checkPermission()) {
            val currentState = state.value
            if (currentState is SearchUiState.Success) {
                state.value = SearchUiState.Success(currentState.updates.toMutableList().setIsInstalling(update.id, true))
            }
            downloadAndInstall(update.id, update.packageName, update.link)
        } else {
            snackBar.snackBar(viewModelScope, TextSnack(stringer.get(R.string.permission_install_required)))
            installer.openInstallSettings()
        }
    }

    override fun sendInstallSnack(log: AppInstallStatus) {
        val currentState = state.value
        if (currentState is SearchUiState.Success) {
            val updates = currentState.updates.toMutableList()
            if (!log.success) {
                state.value = SearchUiState.Success(updates.setError(log.id, log.errorMessage ?: stringer.get(R.string.install_failure, "")))
            }
        }

        if (log.snack) {
            state.value.updates().find { log.id == it.id }?.let { app ->
                val message = if (log.success) R.string.install_success else R.string.install_failure
                val text = if (log.success) stringer.get(message, app.name) 
                           else log.errorMessage ?: stringer.get(message, app.name)
                snackBar.snackBar(viewModelScope, TextSnack(text))
            }
        }
    }

    override fun updateAppStatus(id: Int, status: String) {
        val currentState = state.value
        if (currentState is SearchUiState.Success) {
            state.value = SearchUiState.Success(currentState.updates.toMutableList().setStatus(id, status))
        }
    }

}
