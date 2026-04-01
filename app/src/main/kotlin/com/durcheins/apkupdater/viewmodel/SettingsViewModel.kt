package com.durcheins.apkupdater.viewmodel

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.durcheins.apkupdater.data.ui.SettingsUiState
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.repository.AppsRepository
import com.durcheins.apkupdater.ui.theme.isDarkTheme
import com.durcheins.apkupdater.util.Clipboard
import com.durcheins.apkupdater.util.Themer
import com.durcheins.apkupdater.util.UpdatesNotification
import com.durcheins.apkupdater.worker.UpdatesWorker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val prefs: Prefs,
    private val notification: UpdatesNotification,
    private val workManager: WorkManager,
	private val clipboard: Clipboard,
	private val appsRepository: AppsRepository,
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create(),
	private val themer: Themer
) : ViewModel() {

	val state = MutableStateFlow<SettingsUiState>(SettingsUiState.Settings)

	fun getNewInstaller() = prefs.newInstaller.get()
	fun setNewInstaller(b: Boolean) = prefs.newInstaller.put(b)
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
	fun getUseApkMirror() = prefs.useApkMirror.get()
	fun setUseApkMirror(b: Boolean) = prefs.useApkMirror.put(b)
	fun getUseGitHub() = prefs.useGitHub.get()
	fun setUseGitHub(b: Boolean) = prefs.useGitHub.put(b)
	fun getUsePlay() = prefs.usePlay.get()
	fun setUsePlay(b: Boolean) = prefs.usePlay.put(b)
	fun getEnableAlarm() = prefs.enableAlarm.get()
	fun getAlarmHour() = prefs.alarmHour.get()
	fun getAlarmFrequency() = prefs.alarmFrequency.get()
	fun getTheme() = prefs.theme.get()

	fun setTheme(theme: Int) {
		prefs.theme.put(theme)
		themer.setTheme(isDarkTheme(theme))
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

	fun copyAppList() = viewModelScope.launch(Dispatchers.IO) {
		appsRepository.getApps().collectLatest { apps ->
			apps.onSuccess {
				clipboard.copy(gson.toJson(it), "App List")
			}
		}
	}

	fun copyAppLogs() = viewModelScope.launch(Dispatchers.IO) {
		val process = Runtime.getRuntime().exec("logcat -d")
		val data = process.inputStream.readBytes()
		clipboard.copy(data.decodeToString(), "App Logs")
	}

}
