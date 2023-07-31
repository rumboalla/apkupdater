package com.apkupdater.repository

import android.util.Log
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.prefs.Prefs
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


class UpdatesRepository(
    private val appsRepository: AppsRepository,
    private val apkMirrorRepository: ApkMirrorRepository,
    private val gitHubRepository: GitHubRepository,
    private val fdroidRepository: FdroidRepository,
    private val aptoideRepository: AptoideRepository,
    private val prefs: Prefs
) {

    fun updates() = flow {
        appsRepository.getApps().collect { result ->
            result.onSuccess { apps ->
                val sources = mutableListOf<Flow<List<AppUpdate>>>()
                if (prefs.useApkMirror.get()) sources.add(apkMirrorRepository.updates(apps))
                if (prefs.useGitHub.get()) sources.add(gitHubRepository.updates(apps))
                if (prefs.useFdroid.get()) sources.add(fdroidRepository.updates(apps))
                if (prefs.useAptoide.get()) sources.add(aptoideRepository.updates(apps))
                sources
                    .combine { updates -> emit(updates.flatMap { it }) }
                    .collect()
            }.onFailure {
                Log.e("UpdatesRepository", "Error getting apps", it)
            }
        }
    }.catch {
        Log.e("UpdatesRepository", "Error getting updates", it)
    }

}
