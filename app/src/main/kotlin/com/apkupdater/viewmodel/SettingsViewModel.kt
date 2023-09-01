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
import com.apkupdater.util.UpdatesNotification
import com.apkupdater.worker.UpdatesWorker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import eu.chainfire.libsuperuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SettingsViewModel(
	private val mainViewModel: MainViewModel,
    private val prefs: Prefs,
    private val notification: UpdatesNotification,
    private val workManager: WorkManager,
	private val clipboard: Clipboard,
	private val appsRepository: AppsRepository,
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
) : ViewModel() {

	val state = MutableStateFlow<SettingsUiState>(SettingsUiState.Settings)

	fun setPortraitColumns(n: Int) = prefs.portraitColumns.put(n)
	fun getPortraitColumns() = prefs.portraitColumns.get()
	fun setLandscapeColumns(n: Int) = prefs.landscapeColumns.put(n)
	fun getLandscapeColumns() = prefs.landscapeColumns.get()
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
	fun getUseAptoide() = prefs.useAptoide.get()
	fun setUseAptoide(b: Boolean) = prefs.useAptoide.put(b)
	fun getUseApkPure() = prefs.useApkPure.get()
	fun setUseApkPure(b: Boolean) = prefs.useApkPure.put(b)
	fun getAndroidTvUi() = prefs.androidTvUi.get()
	fun setAndroidTvUi(b: Boolean) = prefs.androidTvUi.put(b)
	fun getEnableAlarm() = prefs.enableAlarm.get()
	fun getRootInstall() = prefs.rootInstall.get()
	fun getAlarmHour() = prefs.alarmHour.get()
	fun getAlarmFrequency() = prefs.alarmFrequency.get()
	fun getTheme() = prefs.theme.get()

	fun setTheme(theme: Int) {
		prefs.theme.put(theme)
		mainViewModel.setTheme(isDarkTheme(theme))
	}

	fun setRootInstall(b: Boolean) {
		if (b && Shell.SU.available()) {
			prefs.rootInstall.put(true)
		} else {
			prefs.rootInstall.put(false)
		}
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

}
