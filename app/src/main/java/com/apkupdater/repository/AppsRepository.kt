package com.apkupdater.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.prefs.Prefs
import com.apkupdater.transform.toAppInstalled
import com.apkupdater.util.orFalse
import kotlinx.coroutines.flow.flow

class AppsRepository(
	private val context: Context,
	private val prefs: Prefs
) {

	private fun excludeSystem() = prefs.excludeSystem.get()
	private fun excludeDisabled() = prefs.excludeDisabled.get()
	private fun excludeStore() = prefs.excludeStore.get()
	private fun ignoredApps() = prefs.ignoredApps.get()

	suspend fun getApps() = flow<Result<List<AppInstalled>>> {
		runCatching {
			val apps = context.packageManager.getInstalledPackages(PackageManager.MATCH_ALL)
				.asSequence()
				.filter { !excludeSystem() || it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
				.filter { !excludeSystem() || it.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0 }
				.filter { !excludeDisabled() || it.applicationInfo.enabled }
				.filter { !excludeStore() || !isAppStore(getInstallerPackageName(it.packageName)) }
				.map { it.toAppInstalled(context, ignoredApps()) }
				.sortedBy { it.name }
				.sortedBy { it.ignored }
				.toList()
			emit(Result.success(apps))
		}.getOrElse {
			emit(Result.failure(it))
		}
	}

	@Suppress("DEPRECATION")
	private fun getInstallerPackageName(packageName: String): String {
		return if (Build.VERSION.SDK_INT < 30) {
			context.packageManager.getInstallerPackageName(packageName).orEmpty()
		} else {
			context.packageManager.getInstallSourceInfo(packageName).installingPackageName.orEmpty()
		}
	}

	// Checks if Play Store or Amazon Store
	private fun isAppStore(name: String?) = name?.contains("com.android.vending").orFalse()
		|| name?.contains("com.amazon").orFalse()

}
