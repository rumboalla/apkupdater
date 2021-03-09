package com.apkupdater.repository

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.repository.apkmirror.ApkMirrorUpdater
import com.apkupdater.repository.apkpure.ApkPureUpdater
import com.apkupdater.repository.aptoide.AptoideUpdater
import com.apkupdater.repository.fdroid.FdroidRepository
import com.apkupdater.repository.googleplay.GooglePlayRepository
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.ioScope
import com.g00fy2.versioncompare.Version
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdatesRepository: KoinComponent {

	private val prefs: AppPrefs by inject()
	private val apkMirrorUpdater: ApkMirrorUpdater by inject()
	private val apkPureUpdater: ApkPureUpdater by inject()
	private val aptoideUpdater: AptoideUpdater by inject()
	private val appsRepository: AppsRepository by inject()
	private val fdroidRepository: FdroidRepository by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()

	fun getUpdatesAsync() = ioScope.async {
		val mutex = Mutex()
		val updates = mutableListOf<AppUpdate>()
		val errors = mutableListOf<Throwable>()

		val apps = appsRepository.getPackageInfosFiltered(PackageManager.GET_SIGNATURES)
		val installedApps = appsRepository.getAppsFiltered(apps)

		val googlePlay = if (prefs.settings.googlePlay) googlePlayRepository.updateAsync(installedApps) else null
		val apkMirror = if (prefs.settings.apkMirror) apkMirrorUpdater.updateAsync(installedApps) else null
		val apkPure = if (prefs.settings.apkPure) apkPureUpdater.updateAsync(installedApps) else null
		val aptoide = if (prefs.settings.aptoide) aptoideUpdater.updateAsync(apps) else null
		val fdroid = if (prefs.settings.fdroid) fdroidRepository.updateAsync(installedApps) else null

		listOfNotNull(apkMirror, apkPure, aptoide, fdroid, googlePlay).forEach {
			it.await().fold(
				onSuccess = { mutex.withLock { updates.addAll(it) } },
				onFailure = { mutex.withLock { errors.add(it) } }
			)
		}

		if (prefs.settings.compareVersionName) filterUpdates(updates, apps)

		if (errors.isEmpty()) Result.success(updates.sortedBy { it.name }) else Result.failure(errors.first())
	}

	private fun filterUpdates(updates: MutableList<AppUpdate>, apps: Sequence<PackageInfo>) {
		updates.retainAll { update ->
			Version(update.version).isHigherThan(apps.find { apk -> apk.packageName == update.packageName }?.versionName)
		}
	}

}
