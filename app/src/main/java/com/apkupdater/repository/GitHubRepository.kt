package com.apkupdater.repository

import android.net.Uri
import android.util.Log
import com.apkupdater.BuildConfig
import com.apkupdater.data.github.GitHubApps
import com.apkupdater.data.github.GitHubRelease
import com.apkupdater.data.github.GitHubReleaseAsset
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitHubSource
import com.apkupdater.data.ui.getApp
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.GitHubService
import com.apkupdater.util.combine
import io.github.g00fy2.versioncompare.Version
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
        val releases = service.getReleases().filter { filterPreRelease(it) }
        val versions = getVersions(releases[0].name)

        if (versions.second > BuildConfig.VERSION_CODE.toLong()) {
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
        val releases = service.getReleases(user, repo).filter { filterPreRelease(it) }
        if (releases.isNotEmpty() && Version(releases[0].tag_name.trimStart('v')) > Version(currentVersion)) {
            val app = apps?.getApp(packageName)
            emit(listOf(AppUpdate(
                name = repo,
                packageName = packageName,
                version = releases[0].tag_name,
                oldVersion = app?.version ?: "?",
                versionCode = 0L,
                oldVersionCode = app?.versionCode ?: 0L,
                source = GitHubSource,
                link = findApkAsset(releases[0].assets),
                whatsNew = releases[0].body,
                iconUri = if (apps == null) Uri.parse(releases[0].author.avatar_url) else Uri.EMPTY
            )))
        } else {
            emit(emptyList())
        }
    }.catch {
        emit(emptyList())
        Log.e("GitHubRepository", "Error fetching releases for $packageName.", it)
    }

    private fun getVersions(name: String) = runCatching {
        val scanner = Scanner(name)
        val version = scanner.next()
        val versionCode = scanner.next().trim('(', ')').toLong()
        Pair(version, versionCode)
    }.getOrDefault(Pair(name, 0L))

    private fun filterPreRelease(release: GitHubRelease) = when {
        prefs.ignorePreRelease.get() && release.prerelease -> false
        else -> true
    }

    private fun findApkAsset(assets: List<GitHubReleaseAsset>) = assets
        .filter { it.browser_download_url.endsWith(".apk", true) }
        .maxByOrNull { it.size }
        ?.browser_download_url
        .orEmpty()

}
