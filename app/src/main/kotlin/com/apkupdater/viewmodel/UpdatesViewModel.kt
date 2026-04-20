package com.apkupdater.viewmodel

import android.content.pm.PackageInstaller
import androidx.lifecycle.viewModelScope
import com.apkupdater.R
import com.apkupdater.data.snack.TextSnack
import com.apkupdater.data.ui.AppInstallStatus
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.UpdatesUiState
import com.apkupdater.data.ui.removeId
import com.apkupdater.data.ui.setIsInstalling
import com.apkupdater.data.ui.setProgress
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.util.Badger
import com.apkupdater.util.Downloader
import com.apkupdater.util.InstallLog
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.SnackBar
import com.apkupdater.util.Stringer
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


class UpdatesViewModel(
	private val updatesRepository: UpdatesRepository,
	private val installer: SessionInstaller,
	private val prefs: Prefs,
	private val badger: Badger,
	downloader: Downloader,
	private val snackBar: SnackBar,
	private val stringer: Stringer,
	installLog: InstallLog
) : InstallViewModel(downloader, installer, prefs, snackBar, stringer, installLog) {

	private val mutex = Mutex()
	private val state = MutableStateFlow<UpdatesUiState>(UpdatesUiState.Loading)

	init {
		subscribeToInstallStatus()
		subscribeToInstallProgress { progress ->
			state.value.onSuccess {
				state.value = UpdatesUiState.Success(it.updates.toMutableList().setProgress(progress))
			}
		}
	}

	fun state(): StateFlow<UpdatesUiState> = state

	fun refresh(load: Boolean = true) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (load) state.value = UpdatesUiState.Loading
		badger.changeUpdatesBadge("")
		updatesRepository.updates().collect {
			setSuccess(it)
		}
	}

	fun installAll() = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if(installer.checkPermission()) {
			state.value.updates().forEach { update ->
				if (state.value.updates().any { it.id == update.id && it.isInstalling }) return@forEach
				state.value = UpdatesUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
				viewModelScope.launch(Dispatchers.IO) {
					downloadAndInstall(update.id, update.packageName, update.link)
				}
			}
		}
	}

	fun ignoreVersion(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		val ignored = prefs.ignoredVersions.get().toMutableList()
		if (ignored.contains(id)) ignored.remove(id) else ignored.add(id)
		prefs.ignoredVersions.put(ignored)
		setSuccess(state.value.updates())
	}

	override fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		state.value.onSuccess {
			state.value = UpdatesUiState.Success(it.updates.toMutableList().setIsInstalling(id, false))
		}
		installer.finish()
	}

	override fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		state.value.onSuccess {
			setSuccess(it.updates.toMutableList().removeId(id))
		}
		installer.finish()
	}

	override fun downloadAndRootInstall(update: AppUpdate) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (state.value.updates().any { it.id == update.id && it.isInstalling }) return@launchWithMutex
		state.value = UpdatesUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
		downloadAndRootInstall(update.id, update.link)
	}

	override fun downloadAndInstall(update: AppUpdate) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		if (state.value.updates().any { it.id == update.id && it.isInstalling }) return@launchWithMutex
		if(installer.checkPermission()) {
			state.value = UpdatesUiState.Success(state.value.mutableUpdates().setIsInstalling(update.id, true))
			downloadAndInstall(update.id, update.packageName, update.link)
		}
	}

	override fun sendInstallSnack(log: AppInstallStatus) {
		if (log.snack) {
			state.value.updates().find { log.id == it.id }?.let { app ->
				val message = if (log.success) R.string.install_success else R.string.install_failure
				snackBar.snackBar(viewModelScope, TextSnack(stringer.get(message, app.name)))
			}
		}
	}

	private fun List<AppUpdate>.filterIgnoredVersions(ignoredVersions: List<Int>) = this
		.filter { !ignoredVersions.contains(it.id) }

	private fun setSuccess(updates: List<AppUpdate>) {
		val currentUpdates = state.value.updates()
		updates
			.filterIgnoredVersions(prefs.ignoredVersions.get())
			.map { newUpdate ->
				currentUpdates.find { it.id == newUpdate.id }?.let { oldUpdate ->
					newUpdate.copy(
						isInstalling = oldUpdate.isInstalling,
						progress = oldUpdate.progress,
						total = oldUpdate.total
					)
				} ?: newUpdate
			}
			.let {
				state.value = UpdatesUiState.Success(it)
				badger.changeUpdatesBadge(it.size.toString())
			}
	}

}
