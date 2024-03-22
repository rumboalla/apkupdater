package com.apkupdater.repository

import android.net.Uri
import android.util.Log
import com.apkupdater.data.codeberg.CodeBergApps
import com.apkupdater.data.codeberg.CodeBergRelease
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.CodeBergSource
import com.apkupdater.data.ui.getApp
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.CodeBergService
import com.apkupdater.util.combine
import com.apkupdater.util.filterVersionTag
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


class CodeBergRepository(
    private val service: CodeBergService,
    private val prefs: Prefs
) {

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val checks = mutableListOf<Flow<List<AppUpdate>>>()
        CodeBergApps.forEach { app ->
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
                source = CodeBergSource,
                link = getApkUrl(packageName, releases[0]),
                whatsNew = releases[0].description,
                iconUri = if (apps == null) Uri.parse(releases[0].author.avatar_url) else Uri.EMPTY
            )))
        } else {
            emit(emptyList())
        }
    }.catch {
        emit(emptyList())
        Log.e("CodeBergRepository", "Error fetching releases for $packageName.", it)
    }

    suspend fun search(text: String) = flow {
        val checks = mutableListOf<Flow<List<AppUpdate>>>()

        CodeBergApps.forEach { app ->
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
        Log.e("CodeBergRepository", "Error searching.", it)
    }

    private fun getApkUrl(
        packageName: String,
        release: CodeBergRelease
    ): String {
        // TODO: Take into account arch
        val source = release.assets.sources.find { it.url.endsWith(".apk", true) }
        if (source != null) return source.url

        val link = release.assets.links.find { it.url.endsWith(".apk", true) }
        if (link != null) return link.url

        return ""
    }

}
