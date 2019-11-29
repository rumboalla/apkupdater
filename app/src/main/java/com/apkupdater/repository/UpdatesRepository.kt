package com.apkupdater.repository

import android.content.pm.PackageManager
import com.apkupdater.model.AppUpdate
import com.apkupdater.repository.apkmirror.ApkMirrorUpdater
import com.apkupdater.repository.aptoide.AptoideUpdater
import com.apkupdater.util.AppPreferences
import com.apkupdater.util.ioScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdatesRepository: KoinComponent {

	private val prefs: AppPreferences by inject()
	private val apkMirrorUpdater: ApkMirrorUpdater by inject()
	private val aptoideUpdater: AptoideUpdater by inject()
	private val appsRepository: AppsRepository by inject()

	fun getUpdatesAsync() = ioScope.async {
		val mutex = Mutex()
		val updates = mutableListOf<AppUpdate>()
		val errors = mutableListOf<Throwable>()

		val apps = appsRepository.getPackageInfosFiltered(PackageManager.GET_SIGNATURES)
		val apkMirror = if (prefs.settings.apkMirror) apkMirrorUpdater.updateAsync(appsRepository.getAppsFiltered(apps)) else null
		val aptoide = if (prefs.settings.aptoide) aptoideUpdater.updateAsync(apps) else null

		listOfNotNull(apkMirror, aptoide).forEach {
			it.await().fold(
				onSuccess = { mutex.withLock { updates.addAll(it) } },
				onFailure = { mutex.withLock { errors.add(it) } }
			)
		}

		if (errors.isEmpty()) Result.success(updates.sortedBy { it.name }) else Result.failure(errors.first())
	}

}