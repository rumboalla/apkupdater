package com.apkupdater.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.apkupdater.data.fdroid.FdroidApp
import com.apkupdater.data.fdroid.FdroidData
import com.apkupdater.data.fdroid.FdroidUpdate
import com.apkupdater.data.fdroid.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.Source
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.FdroidService
import com.apkupdater.util.getSignatureSha256
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.util.jar.JarInputStream


class FdroidRepository(
    private val context: Context,
    private val service: FdroidService,
    private val url: String,
    private val source: Source,
    private val prefs: Prefs
) {
    private val arch = Build.SUPPORTED_ABIS.toSet()
    private val api = Build.VERSION.SDK_INT

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val response = service.getJar("${url}index-v1.jar")
        val data = jarToJson(response.byteStream())
        val appMap = apps.associateBy { it.packageName }
        val updates = data.apps
            .asSequence()
            .mapNotNull { app ->
                appMap[app.packageName]?.let { installed ->
                    data.packages[app.packageName]?.firstOrNull()
                        ?.takeIf { it.versionCode > installed.versionCode && filterSignature(installed, app) }
                        ?.let { latestPkg -> FdroidUpdate(latestPkg, app) }
                }
            }
            .parseUpdates(appMap)
        emit(updates)
    }.catch {
        emit(emptyList())
        Log.e("FdroidRepository", "Error looking for updates.", it)
    }

    suspend fun search(text: String) = flow {
        val response = service.getJar("${url}index-v1.jar")
        val data = jarToJson(response.byteStream())
        val updates = data.apps
            .asSequence()
            .map { FdroidUpdate(data.packages[it.packageName]!![0], it) }
            .filter { it.app.name.contains(text, true) || it.app.packageName.contains(text, true) || it.apk.apkName.contains(text, true) }
            .parseUpdates(null)
        emit(Result.success(updates))
    }.catch {
        emit(Result.failure(it))
        Log.e("FdroidRepository", "Error searching.", it)
    }

    private fun Sequence<FdroidUpdate>.parseUpdates(apps: Map<String, AppInstalled>?) = this
        .filter { it.apk.minSdkVersion <= api }
        .filter { filterArch(it) }
        .filter { filterAlpha(it) }
        .filter { filterBeta(it) }
        .map { it.toAppUpdate(apps?.get(it.app.packageName), source, url) }
        .toList()

    private fun filterSignature(installed: AppInstalled, update: FdroidApp) = when {
        update.allowedAPKSigningKeys.isEmpty() -> true
        update.allowedAPKSigningKeys.contains(context.getSignatureSha256(installed.packageName)) -> true
        else -> false
    }

    private fun filterAlpha(update: FdroidUpdate) = when {
        prefs.ignoreAlpha.get() && update.apk.versionName.contains("alpha", true) -> false
        else -> true
    }

    private fun filterBeta(update: FdroidUpdate) = when {
        prefs.ignoreBeta.get() && update.apk.versionName.contains("beta", true) -> false
        else -> true
    }

    private fun filterArch(update: FdroidUpdate) = when {
        update.apk.nativecode.isEmpty() -> true
        update.apk.nativecode.intersect(arch).isNotEmpty() -> true
        else -> false
    }

    private fun jarToJson(stream: InputStream): FdroidData {
        val jar = JarInputStream(stream)
        var entry = jar.nextJarEntry
        val json = Json { ignoreUnknownKeys = true }
        while (entry != null) {
            if (entry.name == "index-v1.json") {
                return json.decodeFromString<FdroidData>(jar.reader().readText())
            }
            entry = jar.nextJarEntry
        }
        return FdroidData()
    }

}
