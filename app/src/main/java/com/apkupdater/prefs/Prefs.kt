package com.apkupdater.prefs

import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.gson.json
import com.kryptoprefs.preferences.KryptoPrefs


class Prefs(prefs: KryptoPrefs): KryptoContext(prefs) {
	val ignoredApps = json("ignoredApps", emptyList<String>())
	val excludeSystem = boolean("excludeSystem", true)
	val excludeDisabled = boolean("excludeDisabled", true)
	val excludeStore = boolean("excludeStore", false)
	val portraitColumns  = int("portraitColumns", 2)
}
