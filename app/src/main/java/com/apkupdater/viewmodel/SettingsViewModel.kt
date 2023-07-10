package com.apkupdater.viewmodel

import androidx.lifecycle.ViewModel
import com.apkupdater.prefs.Prefs

class SettingsViewModel(
	private val prefs: Prefs
) : ViewModel() {

	fun setPortraitColumns(n: Int) = prefs.portraitColumns.put(n)
	fun getPortraitColumns() = prefs.portraitColumns.get()

}
