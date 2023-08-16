package com.apkupdater.repository

import android.net.Uri
import android.util.Log
import com.apkupdater.BuildConfig
import com.apkupdater.data.github.GitHubApps
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitHubSource
import com.apkupdater.data.ui.getApp
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.GitHubService
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.Flow
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

        GitHubApps.forEachIndexed { i, app ->
            if (i != 0) {
                apps.find { it.packageName == app.packageName }?.let {
                    checks.add(checkApp(apps, app.user, app.repo, app.packageName, it.version))
                }
            }
        }

        checks.combine { all ->
            emit(all.flatMap { it })
        }.collect()
    }.catch {
        emit(emptyList())
        Log.e("GitHubRepository", "Error fetching releases.", it)
    }

    suspend fun search(text: String) = flow {
        val checks = mutableListOf<Flow<List<AppUpdate>>>()

        GitHubApps.forEach { app ->
            if (app.repo.contains(text, true) || app.user.contains(text, true) || app.packageName.contains(text, true)) {
                checks.add(checkApp(null, app.user, app.repo, app.packageName, "?"))
            }
        }

        if (checks.isEmpty()) {
            emit(Result.success(emptyList()))
        } else {
            checks.combine { all ->
                val r = all.flatMap { it }
                emit(Result.success(r))
            }.collect()
        }
    }.catch {
        emit(Result.failure(it))
        Log.e("GitHubRepository", "Error searching.", it)
    }

    private fun selfCheck() = flow {
        val releases = service.getReleases()
        val versions = getVersions(releases[0].name)

        if (versions.second != BuildConfig.VERSION_CODE.toLong()) {
            emit(listOf(AppUpdate(
                name = "APKUpdater",
                packageName = BuildConfig.APPLICATION_ID,
                version = versions.first,
                oldVersion = BuildConfig.VERSION_NAME,
                versionCode = versions.second,
                oldVersionCode = BuildConfig.VERSION_CODE.toLong(),
                source = GitHubSource,
                link = releases[0].assets[0].browser_download_url,
                whatsNew = releases[0].body
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
        apps: List<AppInstalled>?,
        user: String,
        repo: String,
        packageName: String,
        currentVersion: String
    ) = flow {
        val release = service.getReleases(user, repo)[0]
        if (!release.tag_name.contains(currentVersion)) {
            val app = apps?.getApp(packageName)
            emit(listOf(AppUpdate(
                name = repo,
                packageName = packageName,
                version = release.tag_name,
                oldVersion = app?.version ?: "?",
                versionCode = 0L,
                oldVersionCode = app?.versionCode ?: 0L,
                source = GitHubSource,
                link = release.assets[0].browser_download_url,
                whatsNew = release.body,
                iconUri = if (apps == null) Uri.parse(release.author.avatar_url) else Uri.EMPTY
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
