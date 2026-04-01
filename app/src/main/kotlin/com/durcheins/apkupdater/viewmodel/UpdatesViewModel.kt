package com.durcheins.apkupdater.viewmodel

import android.content.pm.PackageInstaller
import androidx.lifecycle.viewModelScope
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.snack.TextSnack
import com.durcheins.apkupdater.data.ui.AppInstallStatus
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.data.ui.UpdatesUiState
import com.durcheins.apkupdater.data.ui.removeId
import com.durcheins.apkupdater.data.ui.setIsInstalling
import com.durcheins.apkupdater.data.ui.setProgress
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.repository.UpdatesRepository
import com.durcheins.apkupdater.util.Badger
import com.durcheins.apkupdater.util.Downloader
import com.durcheins.apkupdater.util.InstallLog
import com.durcheins.apkupdater.util.SessionInstaller
import com.durcheins.apkupdater.util.SnackBar
import com.durcheins.apkupdater.util.Stringer
import com.durcheins.apkupdater.util.launchWithMutex
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
			state.value = UpdatesUiState.Success(state.value.mutableUpdates().setProgress(progress))
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
		setSuccess(state.value.mutableUpdates())
	}

	override fun cancelInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		state.value = UpdatesUiState.Success(state.value.mutableUpdates().setIsInstalling(id, false))
		installer.finish()
	}

	override fun finishInstall(id: Int) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
		setSuccess(state.value.mutableUpdates().removeId(id))
		installer.finish()
	}

	override fun downloadAndInstall(update: AppUpdate) = viewModelScope.launch(Dispatchers.IO) {
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

	private fun setSuccess(updates: List<AppUpdate>) = updates
		.filterIgnoredVersions(prefs.ignoredVersions.get())
		.let {
			state.value = UpdatesUiState.Success(it)
			badger.changeUpdatesBadge(it.size.toString())
		}

}
