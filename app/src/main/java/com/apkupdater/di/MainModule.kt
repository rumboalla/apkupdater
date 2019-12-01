package com.apkupdater.di

import com.apkupdater.repository.AppsRepository
import com.apkupdater.repository.SearchRepository
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.repository.apkmirror.ApkMirrorSearch
import com.apkupdater.repository.apkmirror.ApkMirrorUpdater
import com.apkupdater.repository.aptoide.AptoideSearch
import com.apkupdater.repository.aptoide.AptoideUpdater
import com.apkupdater.repository.fdroid.FdroidRepository
import com.apkupdater.util.AlarmUtil
import com.apkupdater.util.AppPreferences
import com.apkupdater.util.InstallUtil
import com.apkupdater.util.NotificationUtil
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import com.kryptoprefs.preferences.KryptoBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

	single { AppPreferences(get(), KryptoBuilder.nocrypt(get(), "${androidContext().packageName}_preferences")) }

	single { AppsRepository(get(), get()) }
	single { UpdatesRepository() }
	single { SearchRepository() }

	single { ApkMirrorUpdater(get()) }
	single { AptoideUpdater(get()) }
	single { FdroidRepository() }

	single { ApkMirrorSearch() }
	single { AptoideSearch() }

	single { NotificationUtil(get()) }
	single { AlarmUtil(get(), get()) }
	single { InstallUtil() }

	viewModel { AppsViewModel() }
	viewModel { UpdatesViewModel() }
	viewModel { MainViewModel() }
	viewModel { SearchViewModel() }

}