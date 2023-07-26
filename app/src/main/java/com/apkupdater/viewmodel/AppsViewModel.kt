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
	private val mainViewModel: MainViewModel,
	private val repository: AppsRepository,
	private val prefs: Prefs
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<AppsUiState>(AppsUiState.Loading)

	fun state(): StateFlow<AppsUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = AppsUiState.Loading
		mainViewModel.changeAppsBadge("")
		repository.getApps().collect {
			it.onSuccess { apps ->
				state.value = AppsUiState.Success(
					apps, prefs.excludeSystem.get(),
					prefs.excludeStore.get(),
					prefs.excludeDisabled.get()
				)
				mainViewModel.changeAppsBadge(apps.size.toString())
			}.onFailure { ex ->
				state.value = AppsUiState.Error
				mainViewModel.changeAppsBadge("!")
				Log.e("InstalledViewModel", "Error getting apps.", ex)
			}
		}
	}

	fun onSystemClick() = viewModelScope.launchWithMutex(mutex, Dispatchers.Default) {
		prefs.excludeSystem.put(!prefs.excludeSystem.get())
		refresh(false)
	}

	fun onAppStoreClick() = viewModelScope.launchWithMutex(mutex, Dispatchers.Default) {
		prefs.excludeStore.put(!prefs.excludeStore.get())
		refresh(false)
	}

	fun onDisabledClick() = viewModelScope.launchWithMutex(mutex, Dispatchers.Default) {
		prefs.excludeDisabled.put(!prefs.excludeDisabled.get())
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
