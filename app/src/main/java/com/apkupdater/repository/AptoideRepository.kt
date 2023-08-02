package com.apkupdater.repository

import android.os.Build
import android.util.Log
import com.apkupdater.data.aptoide.App
import com.apkupdater.data.aptoide.ListAppsUpdatesRequest
import com.apkupdater.data.aptoide.ListSearchAppsRequest
import com.apkupdater.data.aptoide.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.toApksData
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.AptoideService
import com.apkupdater.util.randomUUID
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class AptoideRepository(
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

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val data = apps.map(AppInstalled::toApksData)
        val r = service.findUpdates(ListAppsUpdatesRequest(data, buildFilterList()))
        emit(r.list.map(App::toAppUpdate))
    }.catch {
        emit(emptyList())
        Log.e("AptoideRepository", "Error looking for updates.", it)
    }

    suspend fun search(text: String) = flow {
        val response = service.searchApps(ListSearchAppsRequest(text, "10", buildFilterList()))
        val updates = response.datalist.list.map(App::toAppUpdate)
        emit(Result.success(updates))
    }.catch {
        emit(Result.failure(it))
        Log.e("AptoideRepository", "Error searching.", it)
    }

    private fun buildFilterList(): String {
        val list = mutableListOf<String>()
        if (prefs.ignoreAlpha.get()) list.add("alpha")
        if (prefs.ignoreBeta.get()) list.add("beta")
        return list.joinToString(separator = ",")
    }

}
