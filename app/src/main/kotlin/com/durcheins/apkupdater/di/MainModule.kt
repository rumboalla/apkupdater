package com.durcheins.apkupdater.di

import androidx.work.WorkManager
import com.durcheins.apkupdater.BuildConfig
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.repository.ApkMirrorRepository
import com.durcheins.apkupdater.repository.AppsRepository
import com.durcheins.apkupdater.repository.GitHubRepository
import com.durcheins.apkupdater.repository.PlayRepository
import com.durcheins.apkupdater.repository.SearchRepository
import com.durcheins.apkupdater.repository.UpdatesRepository
import com.durcheins.apkupdater.service.ApkMirrorService
import com.durcheins.apkupdater.service.GitHubService
import com.durcheins.apkupdater.util.Badger
import com.durcheins.apkupdater.util.Clipboard
import com.durcheins.apkupdater.util.Downloader
import com.durcheins.apkupdater.util.InstallLog
import com.durcheins.apkupdater.util.SessionInstaller
import com.durcheins.apkupdater.util.SnackBar
import com.durcheins.apkupdater.util.Stringer
import com.durcheins.apkupdater.util.Themer
import com.durcheins.apkupdater.util.UpdatesNotification
import com.durcheins.apkupdater.util.addUserAgentInterceptor
import com.durcheins.apkupdater.util.play.PlayHttpClient
import com.durcheins.apkupdater.viewmodel.AppsViewModel
import com.durcheins.apkupdater.viewmodel.MainViewModel
import com.durcheins.apkupdater.viewmodel.SearchViewModel
import com.durcheins.apkupdater.viewmodel.SettingsViewModel
import com.durcheins.apkupdater.viewmodel.UpdatesViewModel
import com.google.gson.GsonBuilder
import com.kryptoprefs.preferences.KryptoBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


val mainModule = module {

	single { GsonBuilder().create() }

	single { Cache(androidContext().cacheDir, 1024 * 1024 * 1024) }

	single {
		HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
	}

	single {
		OkHttpClient.Builder()
			.cache(get())
			.addUserAgentInterceptor("APKUpdater-v" + BuildConfig.VERSION_NAME)
			//.addInterceptor(get<HttpLoggingInterceptor>())
			.build()
	}

	single {
		Retrofit.Builder()
			.client(get())
			.baseUrl("https://www.apkmirror.com")
			.addConverterFactory(GsonConverterFactory.create(get()))
			.build()
			.create(ApkMirrorService::class.java)
	}

	single {
		Retrofit.Builder()
			.client(get())
			.baseUrl("https://api.github.com")
			.addConverterFactory(GsonConverterFactory.create(get()))
			.build()
			.create(GitHubService::class.java)
	}

	single {
		val client = OkHttpClient.Builder().followRedirects(true).cache(get()).build()
		val auroraClient = OkHttpClient.Builder().followRedirects(true).cache(get()).addUserAgentInterceptor("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36").build()
		val dir = File(androidContext().cacheDir, "downloads").apply { mkdirs() }
		Downloader(client, auroraClient, dir)
	}

	single { ApkMirrorRepository(get(), get(), androidContext().packageManager) }

	single { AppsRepository(get(), get()) }

	single { GitHubRepository(get(), get()) }

	single { PlayRepository(get(), get(), get(), get()) }

	single { UpdatesRepository(get(), get(), get(), get(), get()) }

	single { SearchRepository(get(), get(), get(), get()) }

	single { KryptoBuilder.nocrypt(get(), androidContext().getString(R.string.app_name)) }

	single { Prefs(get()) }

	single { UpdatesNotification(get()) }

	single { Clipboard(androidContext()) }

	single { SessionInstaller(get(), get(), get()) }

	single { SnackBar() }

	single { Badger() }

	single { Themer(get()) }

	single { Stringer(androidContext()) }

	single { InstallLog() }

	single { PlayHttpClient(get()) }

	viewModel { MainViewModel(get(), get()) }

	viewModel { AppsViewModel(get(), get(), get()) }

	viewModel { UpdatesViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

	viewModel { SettingsViewModel(get(), get(), WorkManager.getInstance(get()), get(), get(), get(), get()) }

	viewModel { SearchViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

}
