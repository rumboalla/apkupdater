package com.apkupdater.repository

import android.net.Uri
import android.util.Log
import com.apkupdater.data.gitlab.GitLabApps
import com.apkupdater.data.gitlab.GitLabRelease
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.GitLabSource
import com.apkupdater.data.ui.Link
import com.apkupdater.data.ui.getApp
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.GitLabService
import com.apkupdater.util.combine
import com.apkupdater.util.filterVersionTag
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


class GitLabRepository(
    private val service: GitLabService,
    private val prefs: Prefs
) {

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val checks = mutableListOf<Flow<List<AppUpdate>>>()
        GitLabApps.forEach { app ->
            apps.find { it.packageName == app.packageName }?.let {
                checks.add(checkApp(apps, app.user, app.repo, app.packageName, it.version, null))
            }
        }
        if (checks.isEmpty()) {
            emit(emptyList())
        } else {
            checks.combine { all -> emit(all.flatMap { it }) }.collect()
        }
    }

    private suspend fun checkApp(
        apps: List<AppInstalled>?,
        user: String,
        repo: String,
        packageName: String,
        currentVersion: String,
        extra: Regex?
    ) = flow {
        val releases = service.getReleases(user, repo)
            .filter { Version(filterVersionTag(it.tag_name)) > Version(currentVersion) }

        if (releases.isNotEmpty()) {
            val app = apps?.getApp(packageName)
            emit(listOf(
                AppUpdate(
                name = repo,
                packageName = packageName,
                version = releases[0].tag_name,
                oldVersion = app?.version ?: "?",
                versionCode = 0L,
                oldVersionCode = app?.versionCode ?: 0L,
                source = GitLabSource,
                link = Link.Url(getApkUrl(packageName, releases[0])),
                whatsNew = releases[0].description,
                iconUri = if (apps == null) Uri.parse(releases[0].author.avatar_url) else Uri.EMPTY
            )))
        } else {
            emit(emptyList())
        }
    }.catch {
        emit(emptyList())
        Log.e("GitLabRepository", "Error fetching releases for $packageName.", it)
    }

    suspend fun search(text: String) = flow {
        val checks = mutableListOf<Flow<List<AppUpdate>>>()

        GitLabApps.forEach { app ->
            if (app.repo.contains(text, true) || app.user.contains(text, true) || app.packageName.contains(text, true)) {
                checks.add(checkApp(null, app.user, app.repo, app.packageName, "?", null))
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
        Log.e("GitLabRepository", "Error searching.", it)
    }

    private fun getApkUrl(
        packageName: String,
        release: GitLabRelease
    ): String {
        val apks = release.assets.sources.map { it.url }.filter { it.endsWith(".apk", true) }
            .plus(release.assets.links.map { it.url }.filter { it.endsWith(".apk", true) })

        if (apks.isEmpty()) return ""
        if (apks.size == 1) return apks.first()

        // Try to match exact arch using filename and delimiter-aware matching
        for (arch in android.os.Build.SUPPORTED_ABIS) {
            val archPattern = Regex("(?i)(^|[._-])" + Regex.escape(arch) + "([._-]|$)")
            apks.firstOrNull { url ->
                val fileName = Uri.parse(url).lastPathSegment ?: url
                archPattern.containsMatchIn(fileName)
            }?.let { return it }
        }

        // Fallback for common arch name variations
        val supportedAbis = android.os.Build.SUPPORTED_ABIS.toSet()
        val fallbackChecks = listOf(
            "arm64-v8a" to { apk: String -> apk.contains("arm64", true) },
            "x86_64" to { apk: String -> apk.contains("x64", true) },
            "armeabi-v7a" to { apk: String -> apk.contains("arm", true) && !apk.contains("arm64", true) }
        )

        for ((abi, predicate) in fallbackChecks) {
            if (supportedAbis.contains(abi)) {
                apks.firstOrNull(predicate)?.let { return it }
            }
        }

        return apks.first()
    }

}
