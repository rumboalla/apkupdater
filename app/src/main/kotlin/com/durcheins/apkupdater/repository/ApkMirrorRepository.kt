package com.durcheins.apkupdater.repository

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.durcheins.apkupdater.data.apkmirror.AppExistsRequest
import com.durcheins.apkupdater.data.apkmirror.AppExistsResponseApk
import com.durcheins.apkupdater.data.apkmirror.AppExistsResponseData
import com.durcheins.apkupdater.data.apkmirror.toAppUpdate
import com.durcheins.apkupdater.data.ui.ApkMirrorSource
import com.durcheins.apkupdater.data.ui.AppInstalled
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.data.ui.Link
import com.durcheins.apkupdater.data.ui.getApp
import com.durcheins.apkupdater.data.ui.getPackageNames
import com.durcheins.apkupdater.data.ui.getSignature
import com.durcheins.apkupdater.data.ui.getVersionCode
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.service.ApkMirrorService
import com.durcheins.apkupdater.util.combine
import com.durcheins.apkupdater.util.orFalse
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup


class ApkMirrorRepository(
    private val service: ApkMirrorService,
    private val prefs: Prefs,
    packageManager: PackageManager
) {

    private val arch = when {
        Build.SUPPORTED_ABIS.contains("x86") -> "x86"
        Build.SUPPORTED_ABIS.contains("x86_64") -> "x86"
        Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "arm"
        Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm"
        else -> "arm"
    }

    private val api = Build.VERSION.SDK_INT

    suspend fun updates(apps: List<AppInstalled>) = flow {
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
                link = Link.Url("$baseUrl${h5[it].selectFirst("a")?.attr("href")}"),
                iconUri = "$baseUrl${img[it].attr("src")}".replace("=32", "=128").toUri(),
                version = "?",
                oldVersion = "?",
                versionCode = 0L,
                oldVersionCode = 0L,
                source = ApkMirrorSource,
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
                .asSequence()
                .filter { filterSignature(it, apps.getSignature(data.pname))}
                .filter { filterArch(it) }
                .filter { it.versionCode > apps.getVersionCode(data.pname) }
                .filter { filterMinApi(it) }
                .filter { filterAndroidTv(it) }
                .filter { filterWearOS(it) }
                .maxByOrNull { it.versionCode }
                ?.toAppUpdate(apps.getApp(data.pname)!!, data.release)
        }

    private fun filterSignature(apk: AppExistsResponseApk, signature: String?) = when {
        apk.signaturesSha1.isNullOrEmpty() -> true
        apk.signaturesSha1.contains(signature) -> true
        else -> false
    }

    private fun filterArch(app: AppExistsResponseApk) = when {
        app.arches.isEmpty() -> true
        app.arches.contains("universal") || app.arches.contains("noarch") -> true
        app.arches.find { a -> Build.SUPPORTED_ABIS.contains(a) } != null -> true
        app.arches.find { a -> a.contains(arch) } != null -> true
        else -> false
    }

    private fun filterAndroidTv(apk: AppExistsResponseApk): Boolean {
        // Filter out standalone AndroidTV apps
        if (apk.capabilities?.contains("leanback_standalone").orFalse()) return false
        return true
    }

    private fun filterWearOS(apk: AppExistsResponseApk): Boolean {
        // For the moment filter out all standalone Wear OS apps
        if (apk.capabilities?.contains("wear_standalone").orFalse()) {
            return false
        }
        return true
    }

    private fun filterMinApi(apk: AppExistsResponseApk) = runCatching {
        when {
            apk.minapi.toInt() > api -> false
            else -> true
        }
    }.getOrDefault(true)

    private fun buildIgnoreList() = mutableListOf<String>().apply {
        if (prefs.ignoreAlpha.get()) add("alpha")
        if (prefs.ignoreBeta.get()) add("beta")
    }

}
