package com.apkupdater.di

import com.google.gson.GsonBuilder
import com.kryptoprefs.preferences.KryptoBuilder
import com.apkupdater.R
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.AppsRepository
import com.apkupdater.service.ApkMirrorService
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.BottomBarViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val mainModule = module {

	single { GsonBuilder().create() }

	single { Cache(androidContext().cacheDir, 5 * 1024 * 1024) }

	single { OkHttpClient.Builder().cache(get()).build() }

	single {
		Retrofit.Builder()
			.client(get())
			.baseUrl("TODO")
			.addConverterFactory(GsonConverterFactory.create(get()))
			.build()
	}

	single { get<Retrofit>().create(ApkMirrorService::class.java) }

	single { AppsRepository(get(), get()) }

	single { KryptoBuilder.hybrid(get(), androidContext().getString(R.string.app_name)) }

	single { Prefs(get()) }

	viewModel { AppsViewModel(get(), get()) }

	viewModel { BottomBarViewModel() }

}
