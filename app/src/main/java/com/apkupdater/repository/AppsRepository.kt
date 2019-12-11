package com.apkupdater.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.iconUri
import com.apkupdater.util.name
import com.apkupdater.util.orFalse
import com.kryptoprefs.invoke

class AppsRepository(private val context: Context, private val prefs: AppPrefs) {

	private val excludeSystem get() = prefs.settings.excludeSystem
	private val excludeDisabled get() = prefs.settings.excludeDisabled
	private val excludeStore get() = prefs.settings.excludeStore
	private val ignoredApps get() = prefs.ignoredApps()

	private fun getPackageInfos(options: Int = 0): Sequence<PackageInfo> = runCatching {
		return context.packageManager.getInstalledPackages(options).asSequence()
			.filter { !excludeSystem || it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
			.filter { !excludeSystem || it.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0 }
			.filter { !excludeDisabled || it.applicationInfo.enabled }
			.filter { !excludeStore || !isAppStore(context.packageManager.getInstallerPackageName(it.packageName)) }
	}.getOrElse {
		Log.e("AppsRepository", "getPackageInfos", it)
		return sequenceOf()
	}

	fun getPackageInfosFiltered(options: Int = 0) = getPackageInfos(options).filter { !ignoredApps.contains(it.packageName) }

	fun getApps(options: Int = 0) = getPackageInfos(options).mapIndexed { i, app ->
		AppInstalled(
			i,
			app.name(context),
			app.packageName,
			app.versionName ?: "",
			app.versionCode,
			iconUri(app.packageName, app.applicationInfo.icon),
			ignoredApps.contains(app.packageName)
		)
	}.sortedBy { it.name }.sortedBy { it.ignored }.toList()

	fun getAppsFiltered(apps: Sequence<PackageInfo>) = apps.mapIndexed { i, app ->
		AppInstalled(
			i,
			app.name(context),
			app.packageName,
			app.versionName ?: "",
			app.versionCode,
			iconUri(app.packageName, app.applicationInfo.icon),
			ignoredApps.contains(app.packageName)
		)
	}.sortedBy { it.name }.sortedBy { it.ignored }

	// Checks if Play Store or Amazon Store
	private fun isAppStore(name: String?) = name?.contains("com.android.vending").orFalse() || name?.contains("com.amazon").orFalse()

}
