package com.apkupdater.repository

import com.apkupdater.model.ui.AppSearch
import com.apkupdater.repository.apkmirror.ApkMirrorSearch
import com.apkupdater.repository.apkpure.ApkPureSearch
import com.apkupdater.repository.aptoide.AptoideSearch
import com.apkupdater.repository.fdroid.FdroidRepository
import com.apkupdater.repository.googleplay.GooglePlayRepository
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.ioScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

class SearchRepository: KoinComponent {

	private val apkMirrorSearch: ApkMirrorSearch by inject()
	private val apkPureSearch: ApkPureSearch by inject()
	private val aptoideSearch: AptoideSearch by inject()
	private val fdroidRepository: FdroidRepository by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()
	private val prefs: AppPrefs by inject()

	fun getSearchResultsAsync(text: String) = ioScope.async {
		val mutex = Mutex()
		val updates = mutableListOf<AppSearch>()
		val errors = mutableListOf<Throwable>()

		val apkMirror = if (prefs.settings.apkMirror) apkMirrorSearch.searchAsync(text) else null
		val apkPure = if (prefs.settings.apkPure) apkPureSearch.searchAsync(text) else null
		val aptoide = if (prefs.settings.aptoide) aptoideSearch.searchAsync(text) else null
		val fdroid = if (prefs.settings.fdroid) fdroidRepository.searchAsync(text) else null
		val googleplay = if (prefs.settings.googlePlay) googlePlayRepository.searchAsync(text) else null

		listOfNotNull(apkMirror, apkPure, aptoide, fdroid, googleplay).forEach {
			it.await().fold(
				onSuccess = { mutex.withLock { updates.addAll(it) } },
				onFailure = { mutex.withLock { errors.add(it) } }
			)
		}

		if (errors.isEmpty()) Result.success(updates.sortedBy { it.name }) else Result.failure(errors.first())
	}

}