package com.apkupdater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.UpdatesUiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.ApkMirrorRepository
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

class SearchViewModel(
    private val appsRepository: AppsRepository,
    private val apkMirrorRepository: ApkMirrorRepository,
    private val prefs: Prefs
) : ViewModel() {

    private val mutex = Mutex()
    private val state = MutableStateFlow<UpdatesUiState>(UpdatesUiState.Success(emptyList()))

    init {  }

    fun state(): StateFlow<UpdatesUiState> = state

    fun search(text: String, load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        apkMirrorRepository.search(text).collect {
            state.value = UpdatesUiState.Success(it)
        }
    }

}
