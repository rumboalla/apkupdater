package com.apkupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.AppsUiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.Badger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppsViewModel(
	private val repository: AppsRepository,
	private val prefs: Prefs,
	private val badger: Badger
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<AppsUiState>(buildLoadingState())
	private var loadingJob: Job? = null
	
	private val _isProcessing = MutableStateFlow(false)
	val isProcessing = _isProcessing.asStateFlow()

	private val _processingMessage = MutableStateFlow("")
	val processingMessage = _processingMessage.asStateFlow()

	fun state(): StateFlow<AppsUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launch {
		mutex.withLock {
			if (load) {
				loadingJob?.cancel()
				loadingJob = viewModelScope.launch {
					smoothLoadingProgress()
				}
			}
			refreshInternal()
		}
	}

	private suspend fun smoothLoadingProgress() {
		var currentProgress = 0f
		state.value = buildLoadingState(currentProgress, "Initializing...")
		
		while (currentProgress < 0.3f) {
			currentProgress += 0.05f
			state.value = buildLoadingState(currentProgress, "Scanning packages...")
			delay(100)
		}

		while (currentProgress < 0.7f) {
			currentProgress += 0.05f
			state.value = buildLoadingState(currentProgress, "Filtering apps...")
			delay(150)
		}

		while (currentProgress < 0.9f) {
			currentProgress += 0.02f
			state.value = buildLoadingState(currentProgress, "Finalizing list...")
			delay(200)
		}
	}

	private suspend fun refreshInternal() = withContext(Dispatchers.IO) {
		badger.changeAppsBadge("")
		
		repository.getApps().collect { result ->
			loadingJob?.cancel()
			result.onSuccess { apps ->
				state.value = AppsUiState.Success(
					apps,
					prefs.excludeSystem.get(),
					prefs.excludeStore.get(),
					prefs.excludeDisabled.get()
				)
				badger.changeAppsBadge(apps.size.toString())
			}.onFailure { ex ->
				state.value = AppsUiState.Error
				badger.changeAppsBadge("!")
				Log.e("InstalledViewModel", "Error getting apps.", ex)
			}
		}
	}

	fun onSystemClick() = viewModelScope.launch {
		_processingMessage.value = "Filtering system apps..."
		_isProcessing.value = true
		
		mutex.withLock {
			withContext(Dispatchers.IO) {
				prefs.excludeSystem.put(!prefs.excludeSystem.get())
				refreshInternal()
			}
		}
		
		_isProcessing.value = false
	}

	fun onAppStoreClick() = viewModelScope.launch {
		_processingMessage.value = "Filtering store apps..."
		_isProcessing.value = true
		
		mutex.withLock {
			withContext(Dispatchers.IO) {
				prefs.excludeStore.put(!prefs.excludeStore.get())
				refreshInternal()
			}
		}
		
		_isProcessing.value = false
	}

	fun onDisabledClick() = viewModelScope.launch {
		_processingMessage.value = "Filtering disabled apps..."
		_isProcessing.value = true
		
		mutex.withLock {
			withContext(Dispatchers.IO) {
				prefs.excludeDisabled.put(!prefs.excludeDisabled.get())
				refreshInternal()
			}
		}

		_isProcessing.value = false
	}

	fun ignore(packageName: String) = viewModelScope.launch {
		mutex.withLock {
			withContext(Dispatchers.Default) {
				val ignored = prefs.ignoredApps.get().toMutableList()
				if (ignored.contains(packageName)) {
					ignored.remove(packageName)
				} else {
					ignored.add(packageName)
				}
				prefs.ignoredApps.put(ignored)
				refreshInternal()
			}
		}
	}

	private fun buildLoadingState(progress: Float = 0f, stage: String = "") = AppsUiState.Loading(
		prefs.excludeSystem.get(),
		prefs.excludeStore.get(),
		prefs.excludeDisabled.get(),
		progress,
		stage
	)

}
