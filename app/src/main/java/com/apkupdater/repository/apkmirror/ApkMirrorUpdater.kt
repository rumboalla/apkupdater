package com.apkupdater.repository.apkmirror

import android.os.Build
import com.apkupdater.BuildConfig
import com.apkupdater.R
import com.apkupdater.model.apkmirror.AppExistsRequest
import com.apkupdater.model.apkmirror.AppExistsResponse
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.ioScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import java.util.*

class ApkMirrorUpdater(private val prefs: AppPrefs): KoinComponent {

	private val baseUrl = "https://www.apkmirror.com"
	private val appExists = "/wp-json/apkm/v1/app_exists/"
	private val user = "api-apkupdater"
	private val token = "rm5rcfruUjKy04sMpyMPJXW8"
	private val userAgent = "APKUpdater-v" + BuildConfig.VERSION_NAME
	private val source = R.drawable.apkmirror_logo
	private val excludeExperimental get() = prefs.settings.excludeExperimental
	private val excludeArch get() = prefs.settings.excludeArch
	private val excludeMinApi get() = prefs.settings.excludeMinApi

	private val arch = when {
		Build.CPU_ABI.contains("mips") -> "mips"
		Build.CPU_ABI.contains("x86") -> "x86"
		Build.CPU_ABI.contains("arm") -> "arm"
		else -> "arm"
	}

	private fun post(apps: List<AppInstalled>) = Fuel
		.post(baseUrl + appExists)
		.header("User-Agent", userAgent)
		.authentication().basic(user, token)
		.jsonBody(AppExistsRequest(apps.map { it.packageName }, if(excludeExperimental) listOf("alpha", "beta") else emptyList()))
		.responseObject<AppExistsResponse>()

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {
		val updates = mutableListOf<AppUpdate>()
		val errors = mutableListOf<Throwable>()
		val jobs = mutableListOf<Job>()
		val mutex = Mutex()
		apps.chunked(100).forEach { chunk ->
			launch {
				post(chunk).third.fold(
					success = { updates.addAll(parseData(it, chunk)) },
					failure = { errors.add(it) }
				)
			}.let { mutex.withLock { jobs.add(it) } }
		}
		jobs.forEach { it.join() }
		if (errors.isEmpty()) Result.success(updates.filter { it.version != it.oldVersion }) else Result.failure(errors.first())
	}

	private fun parseData(response: AppExistsResponse, apps: List<AppInstalled>) = response.data.mapNotNull { data ->
		data.apks.mapNotNull {
			when {
				!excludeArch -> it
				it.arches.isEmpty() -> it
				it.arches.contains("universal") || it.arches.contains("noarch") -> it
				it.arches.find { a -> a.contains(arch) }?.length ?: 0 > 0 -> it
				else -> null
			}
		}.mapNotNull {
			when {
				!excludeMinApi -> it
				minApiToInt(it.minapi) < Build.VERSION.SDK_INT -> it
				else -> null
			}
		}.filter { it.versionCode > apps.find { app -> app.packageName == data.pname }?.versionCode ?: 0 }
		.takeIf { it.isNotEmpty() }?.reduce { a, b ->
			when {
				a.arches.contains(Build.CPU_ABI) -> a
				a.arches.find { ar -> ar.contains(arch) }?.length ?: 0 > 0 -> a
				else -> b
			}
		}?.let {
			apps.find { a -> a.packageName == data.pname }?.let { app -> AppUpdate.from(app, data, it, baseUrl + it.link, source) }
		}
	}

	private fun minApiToInt(api: String): Int = when {
		api.toLowerCase(Locale.US) == "q" -> 29
		api.toLowerCase(Locale.US) == "p" -> 28
		api.toLowerCase(Locale.US) == "o" -> 26
		else -> api.toIntOrNull() ?: 0
	}

}