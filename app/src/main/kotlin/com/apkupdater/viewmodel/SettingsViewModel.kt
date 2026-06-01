package com.apkupdater.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.apkupdater.data.ui.SettingsUiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.AppsRepository
import com.apkupdater.ui.theme.isDarkTheme
import com.apkupdater.util.Clipboard
import com.apkupdater.util.InstallLog
import com.apkupdater.util.Themer
import com.apkupdater.util.UpdatesNotification
import com.apkupdater.worker.UpdatesWorker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val prefs: Prefs,
    private val notification: UpdatesNotification,
    private val workManager: WorkManager,
	private val clipboard: Clipboard,
	private val appsRepository: AppsRepository,
	private val installLog: InstallLog,
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create(),
	private val themer: Themer
) : ViewModel() {

	val state = MutableStateFlow<SettingsUiState>(SettingsUiState.Settings)
	
	private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
	val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

	fun setPortraitColumns(n: Int) = prefs.portraitColumns.put(n)
	fun getPortraitColumns() = prefs.portraitColumns.get()
	fun setLandscapeColumns(n: Int) = prefs.landscapeColumns.put(n)
	fun getLandscapeColumns() = prefs.landscapeColumns.get()
	fun setPlayTextAnimations(b: Boolean) = prefs.playTextAnimations.put(b)
	fun getPlayTextAnimations() = prefs.playTextAnimations.get()
	fun setIgnoreAlpha(b: Boolean) = prefs.ignoreAlpha.put(b)
	fun getIgnoreAlpha() = prefs.ignoreAlpha.get()
	fun setIgnoreBeta(b: Boolean) = prefs.ignoreBeta.put(b)
	fun getIgnoreBeta() = prefs.ignoreBeta.get()
	fun setIgnorePreRelease(b: Boolean) = prefs.ignorePreRelease.put(b)
	fun getIgnorePreRelease() = prefs.ignorePreRelease.get()
	fun getUseSafeStores() = prefs.useSafeStores.get()
	fun setUseSafeStores(b: Boolean) = prefs.useSafeStores.put(b)
	fun getUseApkMirror() = prefs.useApkMirror.get()
	fun setUseApkMirror(b: Boolean) = prefs.useApkMirror.put(b)
	fun getUseFdroid() = prefs.useFdroid.get()
	fun setUseFdroid(b: Boolean) = prefs.useFdroid.put(b)
	fun getUseIzzy() = prefs.useIzzy.get()
	fun setUseIzzy(b: Boolean) = prefs.useIzzy.put(b)
	fun getUseGitHub() = prefs.useGitHub.get()
	fun setUseGitHub(b: Boolean) = prefs.useGitHub.put(b)
	fun getUseGitLab() = prefs.useGitLab.get()
	fun setUseGitLab(b: Boolean) = prefs.useGitLab.put(b)
	fun getUseAptoide() = prefs.useAptoide.get()
	fun setUseAptoide(b: Boolean) = prefs.useAptoide.put(b)
	fun getUseApkPure() = prefs.useApkPure.get()
	fun setUseApkPure(b: Boolean) = prefs.useApkPure.put(b)
	fun getUsePlay() = prefs.usePlay.get()
	fun setUsePlay(b: Boolean) = prefs.usePlay.put(b)
	fun getAndroidTvUi() = prefs.androidTvUi.get()
	fun setAndroidTvUi(b: Boolean) = prefs.androidTvUi.put(b)
	fun getEnableAlarm() = prefs.enableAlarm.get()
	fun getRootInstall() = prefs.rootInstall.get()
	fun getAlarmHour() = prefs.alarmHour.get()
	fun getAlarmFrequency() = prefs.alarmFrequency.get()
	fun getTheme() = prefs.theme.get()
	fun getBetaTesting() = prefs.betaTesting.get()

	private val _rootStatus = MutableStateFlow<Boolean>(prefs.rootInstall.get())
	val rootStatus = _rootStatus

    private val _betaStatus = MutableStateFlow<Boolean>(prefs.betaTesting.get())
    val betaStatus = _betaStatus

	fun setTheme(theme: Int) {
		prefs.theme.put(theme)
		themer.setTheme(isDarkTheme(theme))
	}

	fun setRootInstall(b: Boolean) {
		if (b) {
			_actionState.value = ActionState.Loading("Requesting Root Access...")
			viewModelScope.launch(Dispatchers.IO) {
				if (Shell.getShell().isRoot) {
					prefs.rootInstall.put(true)
					_rootStatus.value = true
					_actionState.value = ActionState.Success("Root Access Granted")
				} else {
					prefs.rootInstall.put(false)
					_rootStatus.value = false
					_actionState.value = ActionState.Error("Root Access Denied or Not Available")
				}
			}
		} else {
			prefs.rootInstall.put(false)
			_rootStatus.value = false
			_actionState.value = ActionState.Idle
		}
	}

    fun setBetaTesting(b: Boolean) {
        prefs.betaTesting.put(b)
        _betaStatus.value = b
    }

	fun setAlarmFrequency(frequency: Int) {
		prefs.alarmFrequency.put(frequency)
		if (getEnableAlarm()) UpdatesWorker.launch(workManager) else UpdatesWorker.cancel(workManager)
	}

	fun setEnableAlarm(b: Boolean, launcher: ManagedActivityResultLauncher<String, Boolean>) {
		prefs.enableAlarm.put(b)
		if (b) {
			notification.checkNotificationPermission(launcher)
			UpdatesWorker.launch(workManager)
		} else {
			UpdatesWorker.cancel(workManager)
		}
	}

	fun setAlarmHour(hour: Int) {
		prefs.alarmHour.put(hour)
		if (getEnableAlarm()) UpdatesWorker.launch(workManager) else UpdatesWorker.cancel(workManager)
	}

	fun setAbout() {
		state.value = SettingsUiState.About
	}

	fun setSettings() {
		state.value = SettingsUiState.Settings
	}

	fun dismissActionState() {
		_actionState.value = ActionState.Idle
	}

	fun copyAppList() = viewModelScope.launch(Dispatchers.IO) {
		_actionState.value = ActionState.Loading("Generating App List...")
		appsRepository.getApps().collectLatest { apps ->
			apps.onSuccess {
				clipboard.copy(gson.toJson(it), "App List")
				_actionState.value = ActionState.Success("App List Copied to Clipboard")
			}.onFailure {
				_actionState.value = ActionState.Error("Failed to Generate App List")
			}
		}
	}

	fun copyAppLogs() = viewModelScope.launch(Dispatchers.IO) {
		_actionState.value = ActionState.Loading("Copying Logs...")
		val logs = installLog.logs().value.joinToString("\n")
		clipboard.copy(logs, "App Logs")
		_actionState.value = ActionState.Success("Logs Copied to Clipboard")
	}

}

sealed class ActionState {
	data object Idle : ActionState()
	data class Loading(val message: String) : ActionState()
	data class Success(val message: String) : ActionState()
	data class Error(val message: String) : ActionState()
}
