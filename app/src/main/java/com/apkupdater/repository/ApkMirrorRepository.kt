package com.apkupdater.repository

import android.os.Build
import android.util.Log
import com.apkupdater.data.apkmirror.AppExistsRequest
import com.apkupdater.data.apkmirror.AppExistsResponseApk
import com.apkupdater.data.apkmirror.AppExistsResponseData
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.getApp
import com.apkupdater.data.ui.getPackageNames
import com.apkupdater.data.ui.getSignature
import com.apkupdater.data.ui.getVersionCode
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
            .map { appExists(it.getPackageNames()) }
            .combine { all -> emit(parseUpdates(all.flatMap { it }, apps)) }
            .collect()
    }

    private fun appExists(apps: List<String>) = flow {
        emit(service.appExists(AppExistsRequest(apps, buildIgnoreList())).data)
    }.catch {
        emit(emptyList())
        Log.e("ApkMirrorRepository", "Error getting updates.", it)
    }

    private fun parseUpdates(updates: List<AppExistsResponseData>, apps: List<AppInstalled>)
    = updates
        .filter { it.exists == true }
        .mapNotNull { data ->
            data.apks
                .filter { filterSignature(it, apps.getSignature(data.pname))}
                .mapNotNull { filterArch(it) }
                .filter { it.versionCode > apps.getVersionCode(data.pname) }
                .maxByOrNull { it.versionCode }
                ?.toAppUpdate(apps.getApp(data.pname)!!)
        }

    private fun filterSignature(apk: AppExistsResponseApk, signature: String?) = when {
        apk.signaturesSha1 == null || apk.signaturesSha1.isEmpty() -> true
        apk.signaturesSha1.contains(signature) -> true
        else -> false
    }

    private fun filterArch(app: AppExistsResponseApk) = when {
        app.arches.isEmpty() -> app
        app.arches.contains("universal") || app.arches.contains("noarch") -> app
        app.arches.find { a -> a.contains(arch) } != null -> app
        else -> null
    }

    private fun buildIgnoreList() = mutableListOf<String>().apply {
        if (prefs.ignoreAlpha.get()) add("alpha")
        if (prefs.ignoreBeta.get()) add("beta")
    }

}
