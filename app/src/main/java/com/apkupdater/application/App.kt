package com.apkupdater.application

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import com.apkupdater.di.mainModule
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class App : Application(), ImageLoaderFactory, KoinComponent {

	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidLogger()
			androidContext(this@App)
			modules(mainModule)
		}
	}

	override fun newImageLoader() = ImageLoader
		.Builder(this)
		.okHttpClient(get<OkHttpClient>())
		.logger(DebugLogger())
		.build()

}
