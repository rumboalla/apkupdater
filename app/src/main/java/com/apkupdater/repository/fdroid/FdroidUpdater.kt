package com.apkupdater.repository.fdroid

import android.content.Context
import android.content.pm.PackageInfo
import com.apkupdater.R
import com.apkupdater.model.AppInstalled
import com.apkupdater.model.AppUpdate
import com.apkupdater.model.fdroid.FdroidData
import com.apkupdater.util.AppPreferences
import com.apkupdater.util.InstallUtil
import com.apkupdater.util.ioScope
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

class FdroidUpdater: KoinComponent {

	private val prefs: AppPreferences by inject()
	private val installer: InstallUtil by inject()
	private val context: Context by inject()

	private val file = File(context.cacheDir, "fdroid")
	private val baseUrl = " https://f-droid.org/repo/"
	private val index = "index-v1.jar"

	private var data: FdroidData? = null

	@Suppress("BlockingMethodInNonBlockingContext")
	fun getDataAsync() = ioScope.async {
		// Head request so we get only the headers
		val last = Fuel.head("$baseUrl$index").response().second.headers["Last-Modified"].first()

		// Check if last changed
		if (last != prefs.lastFdroid()) {
			// Download new file
			installer.downloadAsync(context, "$baseUrl$index", file)
			prefs.lastFdroid(last)
		}

		// Read the json from inside jar
		if (data == null) {
			val jar = JarFile(file)
			val entry = jar.getEntry("index-v1.json")
			val stream = jar.getInputStream(entry)
			data = Gson().fromJson<FdroidData>(JsonReader(InputStreamReader(stream, "UTF-8")), FdroidData::class.java)
		}

		data
	}

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async{
		runCatching {
			getDataAsync().await()

			apps.mapNotNull { app ->
				data?.packages?.get(app.packageName)?.first()?.let { pack ->
					if (pack.versionCode > app.versionCode) {
						AppUpdate(app.name, app.packageName, pack.versionName, pack.versionCode, app.version, app.versionCode, "$baseUrl${pack.apkName}", R.drawable.fdroid_logo)
					} else null
				}
			}.sortedBy { it.name }.toList()
		}.onFailure { Result.failure<List<AppUpdate>>(it) }.onSuccess { Result.success(it) }
	}

}