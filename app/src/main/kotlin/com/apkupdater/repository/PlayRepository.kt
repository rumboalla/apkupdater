package com.apkupdater.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.PlaySource
import com.apkupdater.prefs.Prefs
import com.apkupdater.util.play.NativeDeviceInfoProvider
import com.apkupdater.util.play.PlayHttpClient
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class PlayRepository(
    private val context: Context,
    private val gson: Gson,
    private val prefs: Prefs
) {
    private suspend fun auth(): AuthData {
        val savedData = prefs.playAuthData.get()
        if (savedData.email.isEmpty()) {
            val properties = NativeDeviceInfoProvider(context).getNativeDeviceProperties()
            val playResponse = PlayHttpClient.postAuth(
                "https://auroraoss.com/api/auth",
                gson.toJson(properties).toByteArray()
            )
            if (playResponse.isSuccessful) {
                val authData = gson.fromJson(String(playResponse.responseBytes), AuthData::class.java)
                prefs.playAuthData.put(authData)
                return authData
            }
            throw IllegalStateException("Auth not successful.")
        }
        return savedData
    }

    suspend fun search(text: String) = flow {
        if (text.contains(" ") || !text.contains(".")) {
            emit(Result.success(emptyList()))
            return@flow
        }
        val authData = auth()
        val details = AppDetailsHelper(authData).using(PlayHttpClient)
        val app = details.getAppByPackageName(text)
        val files = PurchaseHelper(authData).purchase(app.packageName, app.versionCode, app.offerType)
        val link = files.joinToString(separator = ",") { it.url }
        val update = AppUpdate(
            app.displayName,
            app.packageName,
            app.versionName,
            "",
            app.versionCode.toLong(),
            0L,
            PlaySource,
            Uri.parse(app.iconArtwork.url),
            link,
            whatsNew = app.changes
        )
        emit(Result.success(listOf(update)))
    }.catch {
        emit(Result.failure(it))
        Log.e("PlayRepository", "Error searching.", it)
    }

}
