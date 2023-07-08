package com.apkupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.UiState
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
	private val state = MutableStateFlow<UiState>(UiState.Loading)

	init { refresh() }

	fun state(): StateFlow<UiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = UiState.Loading
		repository.getApps().collect {
			it.onSuccess { apps ->
				state.value = UiState.Success(apps, prefs.excludeSystem.get())
			}.onFailure { ex ->
				state.value = UiState.Error
				Log.e("InstalledViewModel", "Error getting apps.", ex)
			}
		}
	}

	fun onSystemClick() {
		prefs.excludeSystem.put(!prefs.excludeSystem.get())
		refresh(false)
	}

	fun ignore(packageName: String) {
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
