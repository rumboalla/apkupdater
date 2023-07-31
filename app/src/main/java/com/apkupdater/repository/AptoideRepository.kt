package com.apkupdater.repository

import android.util.Log
import com.apkupdater.data.aptoide.App
import com.apkupdater.data.aptoide.ListAppsUpdatesRequest
import com.apkupdater.data.aptoide.ListSearchAppsRequest
import com.apkupdater.data.aptoide.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.toApksData
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.AptoideService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class AptoideRepository(
    private val service: AptoideService,
    private val prefs: Prefs
) {

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
