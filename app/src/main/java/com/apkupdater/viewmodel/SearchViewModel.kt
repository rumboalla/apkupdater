package com.apkupdater.viewmodel

import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.data.ui.removeId
import com.apkupdater.data.ui.setIsInstalling
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.SearchRepository
import com.apkupdater.util.Downloader
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


class SearchViewModel(
    private val mainViewModel: MainViewModel,
    private val searchRepository: SearchRepository,
    private val installer: SessionInstaller,
    downloader: Downloader,
    prefs: Prefs
) : InstallViewModel(mainViewModel, downloader, installer, prefs) {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    private var job: Job? = null

    init {
        subscribeToInstallLog { success, id ->
            sendInstallSnack(state.value.updates(), success, id)
        }
    }

    fun state(): StateFlow<SearchUiState> = state

    fun search(text: String) {
        job?.cancel()
        job = searchJob(text)
    }

    private fun searchJob(text: String) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Loading
        mainViewModel.changeSearchBadge("")
        searchRepository.search(text).collect {
            it.onSuccess { apps ->
                state.value = SearchUiState.Success(apps)
                mainViewModel.changeSearchBadge(apps.size.toString())
            }.onFailure {
                mainViewModel.changeSearchBadge("!")
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
        mainViewModel.changeSearchBadge(updates.size.toString())
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
