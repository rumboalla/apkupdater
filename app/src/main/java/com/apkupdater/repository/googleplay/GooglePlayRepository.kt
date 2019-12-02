package com.apkupdater.repository.googleplay

import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.util.ioScope
import kotlinx.coroutines.async
import org.koin.core.KoinComponent


@Suppress("BlockingMethodInNonBlockingContext")
class GooglePlayRepository: KoinComponent {

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {

	}

}