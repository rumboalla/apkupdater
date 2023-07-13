package com.apkupdater.di

import com.apkupdater.BuildConfig
import com.google.gson.GsonBuilder
import com.kryptoprefs.preferences.KryptoBuilder
import com.apkupdater.R
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.ApkMirrorRepository
import com.apkupdater.repository.AppsRepository
import com.apkupdater.service.ApkMirrorService
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.BottomBarViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.SettingsViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val mainModule = module {

	single { GsonBuilder().create() }

	single { Cache(androidContext().cacheDir, 5 * 1024 * 1024) }

	single {
		HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
	}

	single { OkHttpClient
		.Builder()
		.cache(get())
		.addNetworkInterceptor { chain ->
			chain.proceed(
				chain.request()
					.newBuilder()
					.header("User-Agent", "APKUpdater-v" + BuildConfig.VERSION_NAME)
					.build()
			)
		}
		//.addInterceptor(get<HttpLoggingInterceptor>())
		.build()
	}

	single(named("apkmirror")) {
		Retrofit.Builder()
			.client(get())
			.baseUrl("https://www.apkmirror.com")
			.addConverterFactory(GsonConverterFactory.create(get()))
			.build()
	}

	single { get<Retrofit>(named("apkmirror")).create(ApkMirrorService::class.java) }

	single { ApkMirrorRepository(get(), get()) }

	single { AppsRepository(get(), get()) }

	single { KryptoBuilder.hybrid(get(), androidContext().getString(R.string.app_name)) }

	single { Prefs(get()) }

	viewModel { AppsViewModel(get(), get()) }

	viewModel { BottomBarViewModel() }

	viewModel { UpdatesViewModel(get(), get(), get()) }

	viewModel { SettingsViewModel(get()) }

	viewModel { SearchViewModel(get(), get(), get()) }

}
