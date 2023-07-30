package com.apkupdater.viewmodel

import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.data.ui.indexOf
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
    private val downloader: Downloader,
    private val installer: SessionInstaller
) : ViewModel() {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    private var job: Job? = null

    init { subscribeToInstallLog() }

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

    fun install(update: AppUpdate, uriHandler: UriHandler) {
        when (update.source) {
            ApkMirrorSource -> uriHandler.openUri(update.link)
            else -> downloadAndInstall(update)
        }
    }

    private fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Success(setIsInstalling(id, false))
        installer.finish()
    }

    private fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        val updates = state.value.mutableUpdates()
        val index = updates.indexOf(id)
        if (index != -1) updates.removeAt(index)
        state.value = SearchUiState.Success(updates)
        installer.finish()
    }

    private fun subscribeToInstallLog() = viewModelScope.launch(Dispatchers.IO) {
        mainViewModel.appInstallLog.collect {
            if (it.success) {
                finishInstall(it.id)
            } else {
                cancelInstall(it.id)
            }
        }
    }

    private fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
        if(installer.checkPermission()) {
            state.value = SearchUiState.Success(setIsInstalling(update.id, true))
            val stream = downloader.downloadStream(update.link)
            if (stream != null) {
                installer.install(update, stream)
            } else {
                cancelInstall(update.id)
            }
        }
    }

    private fun setIsInstalling(id: Int, b: Boolean): List<AppUpdate> {
        val updates = state.value.mutableUpdates()
        val index = updates.indexOf(id)
        if (index != -1) {
            updates[index] = updates[index].copy(isInstalling = b)
        }
        return updates
    }

}
