package com.apkupdater.repository

import android.util.Log
import com.apkupdater.data.apkpure.AppInfoForUpdate
import com.apkupdater.data.apkpure.DeviceHeader
import com.apkupdater.data.apkpure.GetAppUpdate
import com.apkupdater.data.apkpure.toAppUpdate
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.getApp
import com.apkupdater.data.ui.getSignature
import com.apkupdater.service.ApkPureService
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApkPureRepository {

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        //.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val service: ApkPureService = Retrofit.Builder()
        .client(client)
        .baseUrl("https://tapi.pureapk.com/")
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
        .create(ApkPureService::class.java)

    private val header = gson.toJson(DeviceHeader())

    suspend fun updates(apps: List<AppInstalled>) = flow {
        val info = apps.map { AppInfoForUpdate(it.packageName, it.versionCode) }
        val r = service.getAppUpdate(header, GetAppUpdate(info))
        val updates = r.app_update_response
            .filter { filterSignature(it.sign, apps.getSignature(it.package_name)) }
            .filter { !it.asset.url.contains("/XAPK") }
            .map { it.toAppUpdate(apps.getApp(it.package_name)) }
        emit(updates)
    }.catch {
        Log.e("ApkPureRepository", it.message, it)
        emit(emptyList())
    }

    suspend fun search(text: String) = flow {
        val response = service.search(header, text)
        val info = response.data.data.mapNotNull { d ->
            d.data.firstOrNull()?.takeIf { !it.ad }?.app_info?.let {
                AppInfoForUpdate(it.package_name, 0L, false)
            }
        }
        val r = service.getAppUpdate(header, GetAppUpdate(info))
        val updates = r.app_update_response
            .filter { !it.asset.url.contains("/XAPK") }
            .map { it.toAppUpdate(null) }
        emit(Result.success(updates))
    }.catch {
        Log.e("ApkPureRepository", it.message, it)
        emit(Result.failure(it))
    }

    private fun filterSignature(signatures: List<String>, signature: String) = when {
        signatures.isEmpty() -> true
        signatures.contains(signature) -> true
        else -> false
    }

}
