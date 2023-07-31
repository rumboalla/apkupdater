package com.apkupdater.repository

import android.util.Log
import com.apkupdater.BuildConfig
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitHubSource
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.GitHubService
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.Scanner

class GitHubRepository(
    private val service: GitHubService,
    private val prefs: Prefs
) {

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val checks = mutableListOf(selfCheck())

        // Look for NewPipe if installed
        apps.find { it.packageName == "org.schabi.newpipe" }?.let {
            checks.add(checkApp("TeamNewPipe", "NewPipe", "org.schabi.newpipe", it.version))
        }

        checks.combine { all ->
            emit(all.flatMap { it })
        }.collect()
    }.catch {
        emit(emptyList())
        Log.e("GitHubRepository", "Error fetching releases.", it)
    }

    private fun selfCheck() = flow {
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
        Log.e("GitHubRepository", "Error checking self-update.", it)
    }

    private fun checkApp(
        user: String,
        repo: String,
        packageName: String,
        currentVersion: String
    ) = flow {
        val release = service.getReleases(user, repo)[0]
        if (!release.tag_name.contains(currentVersion)) {
            emit(listOf(AppUpdate(
                name = repo,
                packageName = packageName,
                version = release.tag_name,
                versionCode = 0L,
                source = GitHubSource,
                link = release.assets[0].browser_download_url
            )))
        } else {
            emit(emptyList())
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
