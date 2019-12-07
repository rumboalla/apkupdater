package com.apkupdater.application

import androidx.multidex.MultiDexApplication
import com.apkupdater.di.mainModule
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.sender.HttpSender
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@AcraCore(buildConfigClass = BuildConfig::class)
@AcraHttpSender(httpMethod = HttpSender.Method.POST, uri = "https://collector.tracepot.com/8ead3e03")
class ApkUpdaterApplication : MultiDexApplication() {

	override fun onCreate() {
		super.onCreate()
		initAcra()
		initKoin()
	}

	private fun initKoin() = startKoin{
		androidLogger()
		androidContext(this@ApkUpdaterApplication)
		modules(mainModule)
	}

	private fun initAcra() = ACRA.init(this)

}
