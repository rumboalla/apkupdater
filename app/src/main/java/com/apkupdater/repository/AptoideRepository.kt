package com.apkupdater.repository

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Base64
import android.util.Log
import com.apkupdater.data.aptoide.ListAppsUpdatesRequest
import com.apkupdater.data.aptoide.ListSearchAppsRequest
import com.apkupdater.data.aptoide.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.getApp
import com.apkupdater.data.ui.getVersion
import com.apkupdater.data.ui.toApksData
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.AptoideService
import com.apkupdater.util.isAndroidTv
import com.apkupdater.util.randomUUID
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class AptoideRepository(
    private val context: Context,
    private val service: AptoideService,
    private val prefs: Prefs
) {
    companion object {
        val UserAgent = "aptoide-9.20.6.1;" + getTerminal() + ";0x0;id:" + randomUUID() + ";;"
        private fun getTerminal() = "${getModel()}(${getProduct()});v${getRelease()};${getArch()}"
        private fun getProduct() = Build.PRODUCT.replace(";", " ")
        private fun getModel() = Build.MODEL.replace(";", " ")
        private fun getRelease() = Build.VERSION.RELEASE.replace(";", " ")
        private fun getArch() = System.getProperty("os.arch")
    }

    private val query: String by lazy {
        computeFilters(context)
    }

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val data = apps.map(AppInstalled::toApksData)
        val r = service
            .findUpdates(ListAppsUpdatesRequest(data, query, buildFilterList(), buildStoreList()))
            .list
            .filter { Version(it.file.vername) > Version(apps.getVersion(it.packageName)) }
        emit(r.map { it.toAppUpdate(apps.getApp(it.packageName)) })
    }.catch {
        emit(emptyList())
        Log.e("AptoideRepository", "Error looking for updates.", it)
    }

    suspend fun search(text: String) = flow {
        val request = ListSearchAppsRequest(text, "10", query, buildFilterList(), buildStoreList())
        val response = service.searchApps(request)
        val updates = response.datalist.list.map{ it.toAppUpdate(null) }
        emit(Result.success(updates))
    }.catch {
        emit(Result.failure(it))
        Log.e("AptoideRepository", "Error searching.", it)
    }

    private fun buildStoreList() = if (prefs.useSafeStores.get()) listOf(15L, 711454L) else emptyList()

    private fun buildFilterList(): String {
        val list = mutableListOf<String>()
        if (prefs.ignoreAlpha.get()) list.add("alpha")
        if (prefs.ignoreBeta.get()) list.add("beta")
        return list.joinToString(separator = ",")
    }

    private fun computeFilters(context: Context): String {
        val filters = "maxSdk=${getSdkVer()}&maxScreen=${getScreenSize()}&maxGles=" +
                "${getGlEs(context)}&myCPU=${getAbis()}&leanback=${hasLeanback(context)}" +
                "&myDensity=${getDensityDpi()}"

        return Base64.encodeToString(filters.toByteArray(), 0)
            .replace("=", "")
            .replace("/", "*")
            .replace("+", "_")
            .replace("\n", "")
    }

    private fun getSdkVer() = Build.VERSION.SDK_INT

    private fun getScreenSize(): String {
        val size = Resources.getSystem().configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val sizes = listOf("notfound", "small", "normal", "large", "xlarge")
        return sizes[size]
    }

    private fun getGlEs(context: Context): String {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.deviceConfigurationInfo.glEsVersion
    }

    private fun getAbis() = Build.SUPPORTED_ABIS.joinToString(separator = ",")

    private fun hasLeanback(context: Context): String = if (context.isAndroidTv()) "1" else "0"

    private fun getDensityDpi(): Int {
        val dpi = Resources.getSystem().displayMetrics.densityDpi
        return when {
            dpi <= 120 -> 120
            dpi <= 160 -> 160
            dpi <= 213 -> 213
            dpi <= 240 -> 240
            dpi <= 320 -> 320
            dpi <= 480 -> 480
            else -> 640
        }
    }

}
