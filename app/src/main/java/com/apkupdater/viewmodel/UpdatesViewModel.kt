package com.apkupdater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.UpdatesUiState
import com.apkupdater.repository.ApkMirrorRepository
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

class UpdatesViewModel(
	private val mainViewModel: MainViewModel,
	private val appsRepository: AppsRepository,
	private val apkMirrorRepository: ApkMirrorRepository,
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<UpdatesUiState>(UpdatesUiState.Loading)

	fun state(): StateFlow<UpdatesUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = UpdatesUiState.Loading
		mainViewModel.changeUpdatesBadge("")
		appsRepository.getApps().collect { response ->
			response.onSuccess { apps ->
				apkMirrorRepository.getUpdates(apps).collect { updates ->
					state.value = UpdatesUiState.Success(updates)
					mainViewModel.changeUpdatesBadge(updates.size.toString())
				}
			}.onFailure {
				mainViewModel.changeUpdatesBadge("!")
				state.value = UpdatesUiState.Error
			}
		}
	}

}
