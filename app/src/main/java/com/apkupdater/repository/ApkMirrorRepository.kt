package com.apkupdater.repository

import com.apkupdater.data.apkmirror.AppExistsRequest
import com.apkupdater.data.apkmirror.AppExistsResponseData
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.ApkMirrorService
import com.apkupdater.util.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class ApkMirrorRepository(
    private val service: ApkMirrorService,
    private val prefs: Prefs
) {

    suspend fun getUpdates(apps: List<String>) = flow {
        apps
            .chunked(50)
            .map { appExists(it) }
            .combine { all -> emit(parseUpdates(all.flatMap { it })) }
            .collect()
    }

    private fun appExists(apps: List<String>) = flow {
        emit(service.appExists(AppExistsRequest(apps)).data)
    }

    private fun parseUpdates(updates: List<AppExistsResponseData>) = updates.
        filter { it.exists == false }

}
