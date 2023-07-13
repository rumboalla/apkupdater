package com.apkupdater.repository

import android.net.Uri
import android.os.Build
import android.util.Log
import com.apkupdater.data.apkmirror.AppExistsRequest
import com.apkupdater.data.apkmirror.AppExistsResponseApk
import com.apkupdater.data.apkmirror.AppExistsResponseData
import com.apkupdater.data.apkmirror.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.getApp
import com.apkupdater.data.ui.getPackageNames
import com.apkupdater.data.ui.getSignature
import com.apkupdater.data.ui.getVersionCode
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.ApkMirrorService
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup


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

    suspend fun search(text: String) = flow {
        val baseUrl = "https://www.apkmirror.com"
        val searchQuery = "/?post_type=app_release&searchtype=app&s="
        val doc = Jsoup.connect("$baseUrl$searchQuery$text").get()
        val row = doc.select("div.appRow")
        val a = row.select("a.byDeveloper")
        val h5 = row.select("h5.appRowTitle").take(a.size)
        val img = row.select("img")
        a.removeAt(0)
        img.removeAt(0)
        val result = (0 until a.size).map {
            AppUpdate(
                name = h5[it].attr("title"),
                link = "$baseUrl${h5[it].selectFirst("a")?.attr("href")}",
                iconUri = Uri.parse("$baseUrl${img[it].attr("src")}".replace("=32", "=128")),
                version = "",
                versionCode = 0L,
                packageName = a[it].text() // Developer name in this case
            )
        }
        emit(Result.success(result))
    }.catch {
        emit(Result.failure(it))
        Log.e("ApkMirrorRepository", "Error searching.", it)
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
