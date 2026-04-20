package com.apkupdater.viewmodel

import androidx.lifecycle.viewModelScope
import com.apkupdater.R
import com.apkupdater.data.snack.TextSnack
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.data.ui.removeId
import com.apkupdater.data.ui.setIsInstalling
import com.apkupdater.data.ui.setProgress
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.SearchRepository
import com.apkupdater.util.Badger
import com.apkupdater.util.Downloader
import com.apkupdater.util.InstallLog
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.SnackBar
import com.apkupdater.util.Stringer
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val installer: SessionInstaller,
    private val badger: Badger,
    downloader: Downloader,
    prefs: Prefs,
    private val snackBar: SnackBar,
    private val stringer: Stringer,
    installLog: InstallLog
) : InstallViewModel(downloader, installer, prefs, snackBar, stringer, installLog) {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    private var job: Job? = null

    init {
        subscribeToInstallStatus()
        subscribeToInstallProgress { progress ->
            state.value.onSuccess {
                state.value = SearchUiState.Success(it.updates.toMutableList().setProgress(progress))
            }
        }
    }

    fun state(): StateFlow<SearchUiState> = state

    fun search(text: String) {
        job?.cancel()
        job = searchJob(text)
    }

    private fun searchJob(text: String) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Loading
        badger.changeSearchBadge("")
        searchRepository.search(text).collect {
            it.onSuccess { apps ->
                val currentUpdates = state.value.updates()
                val merged = apps.map { newUpdate ->
                    currentUpdates.find { it.id == newUpdate.id }?.let { oldUpdate ->
                        newUpdate.copy(
                            isInstalling = oldUpdate.isInstalling,
                            progress = oldUpdate.progress,
                            total = oldUpdate.total
                        )
                    } ?: newUpdate
                }
                state.value = SearchUiState.Success(merged)
                badger.changeSearchBadge(merged.size.toString())
            }.onFailure {
                badger.changeSearchBadge("!")
                state.value = SearchUiState.Error
            }
        }
    }

    override fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value.onSuccess {
            state.value = SearchUiState.Success(it.updates.toMutableList().setIsInstalling(id, false))
        }
        installer.finish()
    }

    override fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value.onSuccess {
            val updates = it.updates.toMutableList().removeId(id)
            state.value = SearchUiState.Success(updates)
            badger.changeSearchBadge(updates.size.toString())
        }
        installer.finish()
    }

    override fun downloadAndRootInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        val shouldInstall = mutex.withLock {
            if (state.value.updates().any { it.packageName == update.packageName && it.isInstalling }) return@withLock false
            state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
            return@withLock true
        }
        if (shouldInstall) {
            downloadAndRootInstall(update.id, update.link)
        }
    }

    override fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        val shouldInstall = mutex.withLock {
            if (state.value.updates().any { it.packageName == update.packageName && it.isInstalling }) return@withLock false
            if(!installer.checkPermission()) return@withLock false
            state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
            return@withLock true
        }
        if (shouldInstall) {
            downloadAndInstall(update.id, update.packageName, update.link)
        }
    }

    override fun sendInstallSnack(log: AppInstallStatus) {
        if (log.snack) {
            state.value.updates().find { log.id == it.id }?.let { app ->
                val message = if (log.success) R.string.install_success else R.string.install_failure
                snackBar.snackBar(viewModelScope, TextSnack(stringer.get(message, app.name)))
            }
        }
    }

}
