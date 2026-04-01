package com.durcheins.apkupdater.viewmodel

import androidx.lifecycle.viewModelScope
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.snack.TextSnack
import com.durcheins.apkupdater.data.ui.AppInstallStatus
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.data.ui.SearchUiState
import com.durcheins.apkupdater.data.ui.removeId
import com.durcheins.apkupdater.data.ui.setIsInstalling
import com.durcheins.apkupdater.data.ui.setProgress
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.repository.SearchRepository
import com.durcheins.apkupdater.util.Badger
import com.durcheins.apkupdater.util.Downloader
import com.durcheins.apkupdater.util.InstallLog
import com.durcheins.apkupdater.util.SessionInstaller
import com.durcheins.apkupdater.util.SnackBar
import com.durcheins.apkupdater.util.Stringer
import com.durcheins.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


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
            state.value = SearchUiState.Success(state.value.mutableUpdates().setProgress(progress))
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
                state.value = SearchUiState.Success(apps)
                badger.changeSearchBadge(apps.size.toString())
            }.onFailure {
                badger.changeSearchBadge("!")
                state.value = SearchUiState.Error
            }
        }
    }

    override fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(id, false))
        installer.finish()
    }

    override fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        val updates = state.value.mutableUpdates().removeId(id)
        state.value = SearchUiState.Success(updates)
        badger.changeSearchBadge(updates.size.toString())
        installer.finish()
    }

    override fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        if(installer.checkPermission()) {
            state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
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
