package com.apkupdater.viewmodel

import androidx.lifecycle.viewModelScope
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


class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val installer: SessionInstaller,
    private val badger: Badger,
    downloader: Downloader,
    prefs: Prefs,
    snackBar: SnackBar,
    stringer: Stringer,
    installLog: InstallLog
) : InstallViewModel(downloader, installer, prefs, snackBar, stringer, installLog) {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    private var job: Job? = null

    init {
        subscribeToInstallStatus { status ->
            sendInstallSnack(state.value.updates(), status)
        }
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

    override fun downloadAndRootInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
        downloadAndRootInstall(update.id, update.link)
    }

    override fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        if(installer.checkPermission()) {
            state.value = SearchUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
            downloadAndInstall(update.id, update.packageName, update.link)
        }
    }

}
