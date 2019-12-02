package com.apkupdater.repository.aptoide

import android.content.Context
import android.content.pm.PackageInfo
import com.apkupdater.R
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.model.aptoide.ApksData
import com.apkupdater.model.aptoide.App
import com.apkupdater.model.aptoide.ListAppUpdatesResponse
import com.apkupdater.model.aptoide.ListAppsUpdatesRequest
import com.apkupdater.util.aptoide.AptoideUtils.computeSha1WithColon
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.ioScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

class AptoideUpdater(private val context: Context): KoinComponent {

	private val baseUrl = "https://ws75.aptoide.com/api/7/"
	private val appUpdates = "listAppsUpdates"
	private val source = R.drawable.aptoide_logo
	private val prefs: AppPrefs by inject()
	private val exclude get() = if(prefs.settings.excludeExperimental) "alpha,beta" else ""

	private fun listAppUpdates(request: ListAppsUpdatesRequest) = Fuel
		.post(baseUrl + appUpdates)
		.jsonBody(request)
		.responseObject<ListAppUpdatesResponse>()

	fun updateAsync(apps: Sequence<PackageInfo>) = ioScope.async {
		val apks = apps.map {
			val signature = it.signatures?.let { signatures -> computeSha1WithColon(signatures[0].toByteArray()) } ?: ""
			ApksData(it.packageName, it.versionCode.toString(), signature)
		}.toList()

		val updates = mutableListOf<AppUpdate>()
		val errors = mutableListOf<Throwable>()
		val jobs = mutableListOf<Job>()
		val mutex = Mutex()
		apks.chunked(100).forEach { chunk ->
			launch {
				listAppUpdates(ListAppsUpdatesRequest(chunk, exclude)).third.fold(
					success = { updates.addAll(parseData(it.list, apps)) },
					failure = { errors.add(it) }
				)
			}.let { mutex.withLock { jobs.add(it) } }
		}
		jobs.forEach { it.join() }

		if (errors.isEmpty()) Result.success(updates) else Result.failure(errors.first())
	}

	private fun parseData(list: List<App>, apps: Sequence<PackageInfo>): List<AppUpdate> = list.mapNotNull { app ->
		apps.find { apk -> apk.packageName == app.packageName }?.let { apk -> AppUpdate.from(context, apk, app, source) }
	}

}