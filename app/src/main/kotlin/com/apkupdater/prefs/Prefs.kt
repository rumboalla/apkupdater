package com.apkupdater.prefs

import com.apkupdater.data.ui.Screen
import com.aurora.gplayapi.data.models.AuthData
import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.preferences.KryptoPrefs
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
data class AppAuthData(val email: String = "", val aasToken: String = "")

fun AuthData.toAppAuthData() = AppAuthData(email, aasToken)
fun AppAuthData.toAuthData() = AuthData(email, aasToken)

interface JsonPreference<T> {
    fun get(): T
    fun put(value: T)
    fun set(value: T) = put(value)
}

class Prefs(
	prefs: KryptoPrefs,
	private val json: Json,
	isAndroidTv: Boolean
): KryptoContext(prefs) {

	private inline fun <reified T> json(key: String, defaultValue: T, backed: Boolean = false): JsonPreference<T> {
		val serializer = serializer<T>()
        val defaultStr = try {
            json.encodeToString(serializer, defaultValue)
        } catch (e: Exception) {
            ""
        }
		val pref = stringPreference(key, defaultStr, backed)
		return object : JsonPreference<T> {
			override fun get(): T = try {
				json.decodeFromString(serializer, pref.get())
			} catch (e: Exception) {
				defaultValue
			}
			override fun put(value: T) {
				pref.put(json.encodeToString(serializer, value))
			}
		}
	}

    // Explicitly define return type as Any because we can't import Preference/KryptoPref apparently, but KryptoContext exposes methods returning it.
    // Actually, let's just rely on type inference.
    private fun stringPreference(key: String, defaultValue: String, backed: Boolean) = string(key, defaultValue, backed)

	val ignoredApps = json("ignoredApps", emptyList<String>(), true)
	val ignoredVersions = json("ignoredVersions", emptyList<Int>(), true)
	val excludeSystem = boolean("excludeSystem", defValue = true, backed = true)
	val excludeDisabled = boolean("excludeDisabled", defValue = true, backed = true)
	val excludeStore = boolean("excludeStore", defValue = false, backed = true)
	val portraitColumns = int("portraitColumns", 3, true)
	val landscapeColumns = int("landscapeColumns", 6, true)
	val playTextAnimations = boolean("playTextAnimations", defValue = true, backed = true)
	val ignoreAlpha = boolean("ignoreAlpha", defValue = true, backed = true)
	val ignoreBeta = boolean("ignoreBeta", defValue = true, backed = true)
	val ignorePreRelease = boolean("ignorePreRelease", defValue = true, backed = true)
	val useSafeStores = boolean("useSafeStores", defValue = true, backed = true)
	val useApkMirror = boolean("useApkMirror", defValue = false, backed = true)
	val useGitHub = boolean("useGitHub", defValue = true, backed = true)
	val useGitLab = boolean("useGitLab", defValue = true, backed = true)
	val useFdroid = boolean("useFdroid", defValue = true, backed = true)
	val useIzzy = boolean("useIzzy", defValue = true, backed = true)
	val useAptoide = boolean("useAptoide", defValue = true, backed = true)
	val useApkPure = boolean("useApkPure", defValue = true, backed = true)
	val usePlay = boolean("usePlay", defValue = true, backed = true)
	val enableAlarm = boolean("enableAlarm", defValue = false, backed = true)
	val alarmHour = int("alarmHour", defValue = 12, backed = true)
	val alarmFrequency = int("alarmFrequency", 0, backed = true)
	val androidTvUi = boolean("androidTvUi", defValue = true, backed = true)
	val rootInstall = boolean("rootInstall", defValue = false, backed = true)
	val theme = int("theme", defValue = 0, backed = true)
	val lastTab = string("lastTab", defValue = Screen.Updates.route, backed = true)
	val playAuthData = json("playAuthData", AppAuthData("", ""), true)
	val lastPlayCheck = long("lastPlayCheck", 0L, true)
}
