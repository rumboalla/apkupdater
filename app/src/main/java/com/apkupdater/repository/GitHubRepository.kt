package com.apkupdater.repository

import android.util.Log
import com.apkupdater.BuildConfig
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitHubSource
import com.apkupdater.service.GitHubService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.Scanner

class GitHubRepository(
    private val service: GitHubService
) {

    suspend fun updates() = flow {
        val releases = service.getReleases()
        val versions = getVersions(releases[0].name)

        if (versions.second > BuildConfig.VERSION_CODE) {
            emit(listOf(AppUpdate(
                name = "ApkUpdater",
                packageName = BuildConfig.APPLICATION_ID,
                version = versions.first,
                versionCode = versions.second,
                source = GitHubSource,
                link = releases[0].assets[0].browser_download_url
            )))
        } else {
            // We need to emit empty so it can be combined later
            emit(listOf())
        }
    }.catch {
        emit(emptyList())
        Log.e("GitHubRepository", "Error fetching releases.", it)
    }

    private fun getVersions(name: String) = runCatching {
        val scanner = Scanner(name)
        val version = scanner.next()
        val versionCode = scanner.next().trim('(', ')').toLong()
        Pair(version, versionCode)
    }.getOrDefault(Pair(name, 0L))

}
