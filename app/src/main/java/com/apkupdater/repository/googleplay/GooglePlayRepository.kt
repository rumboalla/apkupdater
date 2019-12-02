package com.apkupdater.repository.googleplay

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.apkupdater.R
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.util.aurora.NativeDeviceInfoProvider
import com.apkupdater.util.aurora.OkHttpClientAdapter
import com.apkupdater.util.ioScope
import com.apkupdater.util.orZero
import com.dragons.aurora.playstoreapiv2.AppDetails
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI
import com.dragons.aurora.playstoreapiv2.PlayStoreApiBuilder
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject

@Suppress("BlockingMethodInNonBlockingContext")
class GooglePlayRepository: KoinComponent {

	private val dispenserUrl = "http://auroraoss.com:8080"

	private val context: Context by inject()
	private val api: GooglePlayAPI by lazy { getApi(context) }

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {
		runCatching {
			val details = api.bulkDetails(apps.map { it.packageName }.toList())
			val updates = mutableListOf<AppUpdate>()

			// TODO: Filter experimental?
			details.entryList.forEach { entry ->
				runCatching {
					apps.getApp(entry.doc.details.appDetails.packageName)?.let { app ->
						if (entry.doc.details.appDetails.versionCode > app.versionCode.orZero()) {
							updates.add(AppUpdate.from(app, entry.doc.details.appDetails))
						}
					}
				}.onFailure { Log.e("GooglePlayRepository", it.message, it) }
			}

			updates
		}.fold(
			onSuccess = { Result.success(it) },
			onFailure = { Result.failure(it) }
		)
	}

	fun getDownloadUrl(packageName: String, versionCode: Int): String {
		val p = api.purchase(packageName, versionCode, 1)
		val d = api.delivery(packageName, versionCode, 1, p.downloadToken)
		return d.appDeliveryData.downloadUrl
	}

	private fun getProvider(context: Context) = NativeDeviceInfoProvider().apply {
		setContext(context)
		setLocaleString(Resources.getSystem().configuration.locale.toString())
	}

	private fun getApi(context: Context): GooglePlayAPI = PlayStoreApiBuilder().apply {
		httpClient = OkHttpClientAdapter(context)
		tokenDispenserUrl = dispenserUrl
		deviceInfoProvider = getProvider(context)
	}.build()

	private fun AppUpdate.Companion.from(app: AppInstalled, entry: AppDetails) = AppUpdate(
		app.name,
		app.packageName,
		entry.versionString,
		entry.versionCode,
		app.version,
		app.versionCode,
		"play",
		R.drawable.googleplay_logo
	)

	private fun Sequence<AppInstalled>.getApp(packageName: String) = this.find { it.packageName == packageName }

}