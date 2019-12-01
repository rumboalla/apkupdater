package com.apkupdater.util

import android.content.Context
import android.content.SharedPreferences
import com.apkupdater.R
import com.apkupdater.model.AppUpdate
import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.gson.json
import com.kryptoprefs.preferences.KryptoPrefs

class AppPreferences(context: Context, prefs: KryptoPrefs): KryptoContext(prefs) {

	val ignoredApps = json(context.getString(R.string.prefs_ignored_apps), emptyList<String>())
	val updates = json(context.getString(R.string.prefs_updates), emptyList<AppUpdate>())
	val settings = PreferenceFragmentPrefs(context, prefs.sharedPreferences())
	val selfUpdateCheck = long("selfUpdateCheck", 0)

}

class PreferenceFragmentPrefs(private val context: Context, private val prefs: SharedPreferences) {

	var excludeSystem
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_system_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_system_key), value).apply()

	var excludeDisabled
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_disabled_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_disabled_key), value).apply()

	var excludeMinApi
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_min_api_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_min_api_key), value).apply()

	var excludeArch
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_arch_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_arch_key), value).apply()

	var excludeExperimental
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_experimental_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_experimental_key), value).apply()

	var checkForUpdates
		get() = prefs.getString(context.getString(R.string.settings_check_for_updates_key), "0")
		set(value) = prefs.edit().putString(context.getString(R.string.settings_check_for_updates_key), value).apply()

	var updateHour
		get() = prefs.getInt(context.getString(R.string.settings_update_hour_key), 12)
		set(value) = prefs.edit().putInt(context.getString(R.string.settings_update_hour_key), value).apply()

	var theme
		get() = prefs.getString(context.getString(R.string.settings_theme_key), "2")
		set(value) = prefs.edit().putString(context.getString(R.string.settings_theme_key), value).apply()

	var apkMirror
		get() = prefs.getBoolean(context.getString(R.string.settings_source_apkmirror_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_source_apkmirror_key), value).apply()

	var aptoide
		get() = prefs.getBoolean(context.getString(R.string.settings_source_aptoide_key), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_source_aptoide_key), value).apply()

	var rootInstall
		get() = prefs.getBoolean(context.getString(R.string.settings_root_install_key), false)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_root_install_key), value).apply()

}