package com.apkupdater.repository.googleplay

import android.content.Context
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.util.aurora.NativeDeviceInfoProvider
import com.apkupdater.util.aurora.OkHttpClientAdapter
import com.apkupdater.util.ioScope
import com.dragons.aurora.playstoreapiv2.PlayStoreApiBuilder
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject


@Suppress("BlockingMethodInNonBlockingContext")
class GooglePlayRepository: KoinComponent {

	private val context: Context by inject()

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {

//		try {
//			val api = PlayStoreApiBuilder().apply {
//				httpClient = OkHttpClientAdapter(context)
//				// TODO: AUTH
//				deviceInfoProvider = NativeDeviceInfoProvider().apply { setContext(context) }
//			}.build()
//			api.details("com.test")
//		} catch (e: Exception) {
//
//		}
//
	}

}