package com.apkupdater.viewmodel

import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.UpdatesUiState
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.util.Downloader
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import com.apkupdater.data.ui.indexOf


class UpdatesViewModel(
	private val mainViewModel: MainViewModel,
	private val updatesRepository: UpdatesRepository,
	private val downloader: Downloader,
	private val installer: SessionInstaller
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<UpdatesUiState>(UpdatesUiState.Loading)

	init { subscribeToInstallLog() }

	fun state(): StateFlow<UpdatesUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = UpdatesUiState.Loading
		mainViewModel.changeUpdatesBadge("")
		updatesRepository.updates().collect {
			state.value = UpdatesUiState.Success(it)
			mainViewModel.changeUpdatesBadge(it.size.toString())
		}
	}

	fun install(update: AppUpdate, uriHandler: UriHandler) {
		when (update.source) {
			ApkMirrorSource -> uriHandler.openUri(update.link)
			else -> downloadAndInstall(update)
		}
	}

	private fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		state.value = UpdatesUiState.Success(setIsInstalling(id, false))
		installer.finish()
	}

	private fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		val updates = state.value.mutableUpdates()
		val index = updates.indexOf(id)
		if (index != -1) updates.removeAt(index)
		state.value = UpdatesUiState.Success(updates)
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
			state.value = UpdatesUiState.Success(setIsInstalling(update.id, true))
			installer.install(update, downloader.download(update.link))
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
