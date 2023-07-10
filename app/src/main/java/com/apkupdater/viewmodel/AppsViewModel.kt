package com.apkupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.AppsUiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

class AppsViewModel(
	private val repository: AppsRepository,
	private val prefs: Prefs
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<AppsUiState>(AppsUiState.Loading)

	init { refresh() }

	fun state(): StateFlow<AppsUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = AppsUiState.Loading
		repository.getApps().collect {
			it.onSuccess { apps ->
				state.value = AppsUiState.Success(apps, prefs.excludeSystem.get())
			}.onFailure { ex ->
				state.value = AppsUiState.Error
				Log.e("InstalledViewModel", "Error getting apps.", ex)
			}
		}
	}

	fun onSystemClick() = viewModelScope.launchWithMutex(mutex, Dispatchers.Default) {
		prefs.excludeSystem.put(!prefs.excludeSystem.get())
		refresh(false)
	}

	fun ignore(packageName: String) = viewModelScope.launchWithMutex(mutex, Dispatchers.Default) {
		val ignored = prefs.ignoredApps.get().toMutableList()
		if (ignored.contains(packageName)) {
			ignored.remove(packageName)
		} else {
			ignored.add(packageName)
		}
		prefs.ignoredApps.put(ignored)
		refresh(false)
	}

}
