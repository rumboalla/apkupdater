package com.durcheins.apkupdater.prefs

import com.durcheins.apkupdater.data.ui.Screen
import com.aurora.gplayapi.data.models.AuthData
import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.gson.json
import com.kryptoprefs.preferences.KryptoPrefs


class Prefs(
	prefs: KryptoPrefs
): KryptoContext(prefs) {
	val ignoredApps = json("ignoredApps", emptyList<String>(), true)
	val ignoredVersions = json("ignoredVersions", emptyList<Int>(), true)
	val excludeSystem = boolean("excludeSystem", defValue = true, backed = true)
	val excludeDisabled = boolean("excludeDisabled", defValue = true, backed = true)
	val excludeStore = boolean("excludeStore", defValue = false, backed = true)
	val portraitColumns = int("portraitColumns", 3, true)
	val landscapeColumns = int("landscapeColumns", 6, true)
	val playTextAnimations = boolean("playTextAnimations", defValue = true, backed = true)
	val ignoreAlpha = boolean("ignoreAlpha", defValue = true, backed = true)
	val ignoreBeta = boolean("ignoreBeta", defValue = false, backed = true)
	val ignorePreRelease = boolean("ignorePreRelease", defValue = false, backed = true)
	val useApkMirror = boolean("useApkMirror", defValue = false, backed = true)
	val useGitHub = boolean("useGitHub", defValue = true, backed = true)
	val usePlay = boolean("usePlay", defValue = true, backed = true)
	val enableAlarm = boolean("enableAlarm", defValue = false, backed = true)
	val alarmHour = int("alarmHour", defValue = 12, backed = true)
	val alarmFrequency = int("alarmFrequency", 0, backed = true)
	val theme = int("theme", defValue = 0, backed = true)
	val lastTab = string("lastTab", defValue = Screen.Updates.route, backed = true)
	val playAuthData = json("playAuthData", AuthData("", ""), true)
	val lastPlayCheck = long("lastPlayCheck", 0L, true)
	val newInstaller = boolean("newInstaller", defValue = false, backed = true)
}
