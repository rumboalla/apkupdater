package com.apkupdater.application

import android.app.Application
import com.apkupdater.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ApkUpdaterApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		startKoin{
			androidLogger()
			androidContext(this@ApkUpdaterApplication)
			modules(mainModule)
		}
	}

}