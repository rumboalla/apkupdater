package com.apkupdater.repository

import android.util.Log
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class UpdatesRepository(
    private val appsRepository: AppsRepository,
    private val apkMirrorRepository: ApkMirrorRepository,
    private val gitHubRepository: GitHubRepository
) {

    fun updates() = flow {
        appsRepository.getApps().collect { result ->
            result.onSuccess { apps ->
                listOf(apkMirrorRepository.updates(apps), gitHubRepository.updates())
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
