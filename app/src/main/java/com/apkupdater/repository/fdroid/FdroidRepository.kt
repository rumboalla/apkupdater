package com.apkupdater.repository.fdroid

import android.content.Context
import android.os.Build
import android.util.Log
import com.apkupdater.R
import com.apkupdater.model.fdroid.FdroidApp
import com.apkupdater.model.fdroid.FdroidData
import com.apkupdater.model.fdroid.FdroidPackage
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.model.ui.AppSearch
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.app.InstallUtil
import com.apkupdater.util.ioScope
import com.apkupdater.util.orZero
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.kryptoprefs.invoke
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.io.InputStreamReader
import java.util.jar.JarFile

class FdroidRepository: KoinComponent {

	private val prefs: AppPrefs by inject()
	private val installer: InstallUtil by inject()
	private val context: Context by inject()

	private val file = File(context.cacheDir, "fdroid")
	private val baseUrl = "https://f-droid.org/repo/"
	private val index = "index-v1.jar"

	private var lastCheck = 0L
	private var data: FdroidData? = null

	private val arch: List<String> by lazy {
		if (Build.VERSION.SDK_INT >= 21) {
			Build.SUPPORTED_ABIS.toList()
		} else {
			listOfNotNull(Build.CPU_ABI, Build.CPU_ABI2)
		}
	}

	@Suppress("BlockingMethodInNonBlockingContext")
	fun getDataAsync() = ioScope.async {
        runCatching {
            if (data == null || System.currentTimeMillis() - lastCheck > 3600000) {
                // Head request so we get only the headers
                var last = ""
                try {
                    last = Fuel.head("$baseUrl$index").response().second.headers["Last-Modified"].first()
                } catch (e: Exception) {

                }

                lastCheck = System.currentTimeMillis()

                // Check if last changed
                var refresh = false
                if (last != prefs.lastFdroid() || data == null) {
                    // Download new file
                    installer.downloadAsync(context, "$baseUrl$index", file)
                    prefs.lastFdroid(last)
                    refresh = true
                }

                // Read the json from inside jar
                if (data == null || refresh) {
                    val jar = JarFile(file)
                    val stream = jar.getInputStream(jar.getEntry("index-v1.json"))
                    data = Gson().fromJson<FdroidData>(JsonReader(InputStreamReader(stream, "UTF-8")), FdroidData::class.java)
                }
            }
            data
        }.fold(
            onSuccess = { it },
            onFailure = {
                Log.e("FdroidRepository", "getDataAsync", it)
                null
            }
        )
	}

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {
		runCatching {
			if (getDataAsync().await() == null) throw NullPointerException("updateAsync: data is null")

			apps.mapNotNull { app -> if (prefs.settings.excludeExperimental && isExperimental(data?.apps?.find { it.packageName == app.packageName }?: FdroidApp())) null else app }
				.mapNotNull { app -> if (prefs.settings.excludeMinApi && data?.packages?.get(app.packageName)?.first()?.minSdkVersion.orZero() > Build.VERSION.SDK_INT) null else app }
				.mapNotNull { app -> if (prefs.settings.excludeArch && isIncompatibleArch(data?.packages?.get(app.packageName)?.first())) null else app }
				.mapNotNull { app -> data?.packages?.get(app.packageName)?.first()?.let { pack -> if (pack.versionCode > app.versionCode) AppUpdate.from(app, pack) else null }
			}.sortedBy { it.name }.toList()
		}.onFailure { Result.failure<List<AppUpdate>>(it) }.onSuccess { Result.success(it) }
	}

	fun searchAsync(text: String) = ioScope.async {
		runCatching {
			getDataAsync().await()?.apps.orEmpty()
				.asSequence()
				.filter { app -> app.description.contains(text, true) || app.packageName.contains(text, true) }
				.mapNotNull { app -> if (prefs.settings.excludeExperimental && isExperimental(app)) null else app }
				.mapNotNull { app -> if (prefs.settings.excludeMinApi && data?.packages?.get(app.packageName)?.first()?.minSdkVersion.orZero() > Build.VERSION.SDK_INT) null else app }
				.mapNotNull { app -> if (prefs.settings.excludeArch && isIncompatibleArch(data?.packages?.get(app.packageName)?.first())) null else app }
				.sortedByDescending { app -> app.lastUpdated }
				.take(10)
				.map { app -> AppSearch.from(app) }
				.toList()
		}.fold(onSuccess = { Result.success(it) }, onFailure = { Result.failure(it) })
	}

	private fun isIncompatibleArch(pack: FdroidPackage?): Boolean {
		pack?.let { return !(it.nativecode.isEmpty() || it.nativecode.intersect(arch).isNotEmpty()) }
		return false
	}

	private fun isExperimental(app: FdroidApp): Boolean {
		if (app.suggestedVersionName.contains("-alpha", true) || app.suggestedVersionName.contains("-beta", true)
			|| app.packageName.endsWith(".alpha") || app.packageName.endsWith(".beta")
			|| app.name.contains("(alpha)", true) || app.name.contains("(beta)", true)) return true
		return false
	}

	private fun AppUpdate.Companion.from(app: AppInstalled, pack: FdroidPackage) =
		AppUpdate(
			app.name,
			app.packageName,
			pack.versionName,
			pack.versionCode,
			app.version,
			app.versionCode,
			"$baseUrl${pack.apkName}",
			R.drawable.fdroid_logo
		)

	private fun AppSearch.Companion.from(app: FdroidApp) =
		AppSearch(
			app.name,
			"$baseUrl${app.packageName}_${app.suggestedVersionCode}.apk",
			"${baseUrl}icons-640/${app.icon}",
			app.packageName,
			R.drawable.fdroid_logo
		)

}