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


class UpdatesViewModel(
	private val mainViewModel: MainViewModel,
	private val updatesRepository: UpdatesRepository,
	private val downloader: Downloader,
	private val installer: SessionInstaller
) : ViewModel() {

	private val mutex = Mutex()
	private val state = MutableStateFlow<UpdatesUiState>(UpdatesUiState.Loading)

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

	private fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
		val apk = downloader.download(update.link)
		installer.install(update.packageName, apk)
	}

}
