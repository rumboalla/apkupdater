package com.apkupdater.repository

import android.os.Build
import android.util.Log
import com.apkupdater.data.apkmirror.AppExistsRequest
import com.apkupdater.data.apkmirror.AppExistsResponseApk
import com.apkupdater.data.apkmirror.AppExistsResponseData
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.ApkMirrorService
import com.apkupdater.transform.toAppUpdate
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


class ApkMirrorRepository(
    private val service: ApkMirrorService,
    private val prefs: Prefs
) {
    private val arch = when {
        Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "arm"
        Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm"
        Build.SUPPORTED_ABIS.contains("x86") -> "x86"
        Build.SUPPORTED_ABIS.contains("x86_64") -> "x86"
        else -> "arm"
    }

    suspend fun getUpdates(apps: List<AppInstalled>) = flow {
        apps.chunked(100)
            .map { appExists(getPackageNames(it)) }
            .combine { all -> emit(parseUpdates(all.flatMap { it }, apps)) }
            .collect()
    }

    private fun appExists(apps: List<String>) = flow {
        emit(service.appExists(AppExistsRequest(apps)).data)
    }.catch {
        emit(emptyList())
        Log.e("ApkMirrorRepository", "Error getting updates.", it)
    }

    private fun parseUpdates(updates: List<AppExistsResponseData>, apps: List<AppInstalled>)
    = updates
        .filter { it.exists == true }
        .mapNotNull { data ->
            data.apks
                .mapNotNull { filterArch(it) }
                .filter { it.versionCode > getVersionCode(apps, data.pname) }
                .maxByOrNull { it.versionCode }
                ?.toAppUpdate(getApp(apps, data.pname)!!)
        }

    private fun getVersionCode(apps: List<AppInstalled>, packageName: String) = apps
        .find { it.packageName == packageName }?.versionCode ?: 0

    private fun getPackageNames(apps: List<AppInstalled>) = apps
        .filter { !it.ignored }
        .map { it.packageName }

    private fun getApp(apps: List<AppInstalled>, packageName: String) = apps
        .find { it.packageName == packageName }

    private fun filterArch(app: AppExistsResponseApk) = when {
        app.arches.isEmpty() -> app
        app.arches.contains("universal") || app.arches.contains("noarch") -> app
        app.arches.find { a -> a.contains(arch) } != null -> app
        else -> null
    }

}
