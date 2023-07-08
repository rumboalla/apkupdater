package com.apkupdater.prefs

import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.gson.json
import com.kryptoprefs.preferences.KryptoPrefs


class Prefs(prefs: KryptoPrefs): KryptoContext(prefs) {
	val ignoredApps = json("ignoredApps", emptyList<String>())
	val excludeSystem get() = boolean("excludeSystem", false)
	val excludeDisabled get() = boolean("excludeDisabled", true)
	val excludeStore get() = boolean("excludeStore", false)
}
