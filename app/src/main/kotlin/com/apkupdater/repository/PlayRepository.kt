package com.apkupdater.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.PlaySource
import com.apkupdater.data.ui.getPackageNames
import com.apkupdater.data.ui.getVersion
import com.apkupdater.data.ui.getVersionCode
import com.apkupdater.prefs.Prefs
import com.apkupdater.util.play.NativeDeviceInfoProvider
import com.apkupdater.util.play.PlayHttpClient
import com.aurora.gplayapi.data.models.App
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
        val app = AppDetailsHelper(authData)
            .using(PlayHttpClient)
            .getAppByPackageName(text)
        val update = app.toAppUpdate(PurchaseHelper(authData))
        emit(Result.success(listOf(update)))
    }.catch {
        emit(Result.failure(it))
        Log.e("PlayRepository", "Error searching.", it)
    }

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val authData = auth()
        val details = AppDetailsHelper(authData)
            .using(PlayHttpClient)
            .getAppByPackageName(apps.getPackageNames())
        val purchaseHelper = PurchaseHelper(authData)
        val updates = details
            .filter { it.versionCode > apps.getVersionCode(it.packageName) }
            .map { it.toAppUpdate(purchaseHelper, apps.getVersion(it.packageName), apps.getVersionCode(it.packageName)) }
        emit(updates)
    }.catch {
        emit(emptyList())
        Log.e("AptoideRepository", "Error looking for updates.", it)
    }

}

fun App.toAppUpdate(
    purchaseHelper: PurchaseHelper,
    oldVersion: String = "",
    oldVersionCode: Long = 0L
) = AppUpdate(
    displayName,
    packageName,
    versionName,
    oldVersion,
    versionCode.toLong(),
    oldVersionCode,
    PlaySource,
    Uri.parse(iconArtwork.url),
    purchaseHelper.purchase(packageName, versionCode, offerType).joinToString(",") { it.url },
    whatsNew = changes
)
