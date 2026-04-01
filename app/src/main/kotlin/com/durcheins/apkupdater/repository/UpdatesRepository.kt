package com.durcheins.apkupdater.repository

import android.util.Log
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.util.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


class UpdatesRepository(
    private val appsRepository: AppsRepository,
    private val apkMirrorRepository: ApkMirrorRepository,
    private val gitHubRepository: GitHubRepository,
    private val playRepository: PlayRepository,
    private val prefs: Prefs
) {

    fun updates() = flow<List<AppUpdate>> {
        appsRepository.getApps().collect { result ->
            result.onSuccess { apps ->
                val filtered = apps.filter { !it.ignored }
                val sources = mutableListOf<Flow<List<AppUpdate>>>()
                if (prefs.useApkMirror.get()) sources.add(apkMirrorRepository.updates(filtered))
                if (prefs.useGitHub.get()) sources.add(gitHubRepository.updates(filtered))
                if (prefs.usePlay.get()) sources.add(playRepository.updates(filtered))

                if (sources.isNotEmpty()) {
                    sources
                        .combine { updates -> emit(updates.flatMap { it }) }
                        .collect()
                } else {
                    emit(emptyList())
                }
            }.onFailure {
                Log.e("UpdatesRepository", "Error getting apps", it)
            }
        }
    }.catch {
        Log.e("UpdatesRepository", "Error getting updates", it)
    }

}
