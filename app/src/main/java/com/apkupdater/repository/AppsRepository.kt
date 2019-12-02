package com.apkupdater.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.util.AppPreferences
import com.apkupdater.util.name
import com.kryptoprefs.invoke

class AppsRepository(private val context: Context, private val prefs: AppPreferences) {

	private val excludeSystem get() = prefs.settings.excludeSystem
	private val excludeDisabled get() = prefs.settings.excludeDisabled
	private val ignoredApps get() = prefs.ignoredApps()

	private fun getPackageInfos(options: Int = 0) = context.packageManager.getInstalledPackages(options).asSequence()
		.filter { !excludeSystem || it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
		.filter { !excludeSystem || it.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0 }
		.filter { !excludeDisabled || it.applicationInfo.enabled }

	fun getPackageInfosFiltered(options: Int = 0) = getPackageInfos(options).filter { !ignoredApps.contains(it.packageName) }

	fun getApps(options: Int = 0) = getPackageInfos(options).mapIndexed { i, app ->
		AppInstalled(
			i,
			app.name(context),
			app.packageName,
			app.versionName ?: "",
			app.versionCode,
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
			ignoredApps.contains(app.packageName)
		)
	}.sortedBy { it.name }.sortedBy { it.ignored }

}
