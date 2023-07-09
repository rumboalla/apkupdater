package com.apkupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.UiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.ApkMirrorRepository
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

class UpdatesViewModel(
	private val appsRepository: AppsRepository,
	private val apkMirrorRepository: ApkMirrorRepository,
	private val prefs: Prefs
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<UiState>(UiState.Loading)

	init { refresh() }

	fun state(): StateFlow<UiState> = state

	private fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		appsRepository.getApps().collect { response ->
			response.onSuccess { apps ->
				apkMirrorRepository.getUpdates(
					apps.map { it.packageName }
				).collect { updates ->
					Log.e("Test", updates.toString())
				}
			}.onFailure {

			}
		}
	}

}
