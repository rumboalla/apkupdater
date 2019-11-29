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
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_system), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_system), value).apply()

	var excludeDisabled
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_disabled), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_disabled), value).apply()

	var excludeMinApi
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_min_api), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_min_api), value).apply()

	var excludeArch
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_arch), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_arch), value).apply()

	var excludeExperimental
		get() = prefs.getBoolean(context.getString(R.string.settings_exclude_experimental), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_exclude_experimental), value).apply()

	var checkForUpdates
		get() = prefs.getBoolean(context.getString(R.string.settings_check_for_updates), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_check_for_updates), value).apply()

	var theme
		get() = prefs.getString(context.getString(R.string.settings_theme), "2")
		set(value) = prefs.edit().putString(context.getString(R.string.settings_theme), value).apply()

	var apkMirror
		get() = prefs.getBoolean(context.getString(R.string.settings_source_apkmirror), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_source_apkmirror), value).apply()

	var aptoide
		get() = prefs.getBoolean(context.getString(R.string.settings_source_aptoide), true)
		set(value) = prefs.edit().putBoolean(context.getString(R.string.settings_source_aptoide), value).apply()

}