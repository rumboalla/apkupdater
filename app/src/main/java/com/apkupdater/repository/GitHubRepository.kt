package com.apkupdater.repository

import android.util.Log
import com.apkupdater.BuildConfig
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitHubSource
import com.apkupdater.service.GitHubService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GitHubRepository(
    private val service: GitHubService
) {

    suspend fun updates() = flow {
        val releases = service.getReleases()
        if (releases[0].name != BuildConfig.VERSION_NAME) {
            emit(listOf(AppUpdate(
                name = "ApkUpdater",
                packageName = "com.apkupdater3",
                version = releases[0].name,
                versionCode = 0L,
                source = GitHubSource,
                link = releases[0].assets[0].browser_download_url
            )))
        }
    }.catch {
        emit(emptyList())
        Log.e("GitHubRepository", "Error fetching releases.", it)
    }

}
