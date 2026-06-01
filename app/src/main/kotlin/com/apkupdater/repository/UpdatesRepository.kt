package com.apkupdater.repository

import android.util.Log
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.prefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart


class UpdatesRepository(
    private val appsRepository: AppsRepository,
    private val apkMirrorRepository: ApkMirrorRepository,
    private val gitHubRepository: GitHubRepository,
    private val fdroidRepository: FdroidRepository,
    private val izzyRepository: FdroidRepository,
    private val aptoideRepository: AptoideRepository,
    private val apkPureRepository: ApkPureRepository,
    private val gitLabRepository: GitLabRepository,
    private val playRepository: PlayRepository,
    private val prefs: Prefs,
) {

    fun updates(): Flow<List<AppUpdate>> = flow {
        appsRepository.getApps().collect { result ->
            result.onSuccess { apps ->
                val filtered = apps.filter { !it.ignored }
                val sources = mutableListOf<Flow<List<AppUpdate>>>()
                
                fun addSource(flow: Flow<List<AppUpdate>>, name: String) {
                    sources.add(
                        flow.onStart { emit(emptyList()) }.catch { e ->
                            Log.e("UpdatesRepository", "Error in source $name", e)
                            emit(emptyList())
                        }
                    )
                }

                if (prefs.useApkMirror.get()) addSource(apkMirrorRepository.updates(filtered), "ApkMirror")
                if (prefs.useGitHub.get()) addSource(gitHubRepository.updates(filtered), "GitHub")
                if (prefs.useFdroid.get()) addSource(fdroidRepository.updates(filtered), "Fdroid")
                if (prefs.useIzzy.get()) addSource(izzyRepository.updates(filtered), "Izzy")
                if (prefs.useAptoide.get()) addSource(aptoideRepository.updates(filtered), "Aptoide")
                if (prefs.useApkPure.get()) addSource(apkPureRepository.updates(filtered), "ApkPure")
                if (prefs.useGitLab.get()) addSource(gitLabRepository.updates(filtered), "GitLab")
                if (prefs.usePlay.get()) addSource(playRepository.updates(filtered), "Play")

                if (sources.isNotEmpty()) {
                    val combinedFlow = combine(sources) { updatesArray ->
                        updatesArray.asSequence()
                            .flatMap { it }
                            .groupBy { it.packageName }
                            .map { (packageName, updates) ->
                                val bestUpdate = updates.maxBy { it.versionCode }
                                val app = apps.find { it.packageName == packageName }
                                bestUpdate.copy(isPersistent = app?.isPersistent ?: false)
                            }
                            .toList()
                    }
                    emitAll(combinedFlow)
                } else {
                    emit(emptyList())
                }
            }.onFailure {
                Log.e("UpdatesRepository", "Error getting apps", it)
                throw it
            }
        }
    }.catch { e ->
        Log.e("UpdatesRepository", "Error in updates flow", e)
        throw e
    }

    fun ignoreVersion(id: Int) {
        val ignored = prefs.ignoredVersions.get().toMutableList()
        ignored.add(id)
        prefs.ignoredVersions.put(ignored)
    }

}
