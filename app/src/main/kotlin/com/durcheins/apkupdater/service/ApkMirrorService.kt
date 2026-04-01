package com.durcheins.apkupdater.service

import com.durcheins.apkupdater.data.apkmirror.AppExistsRequest
import com.durcheins.apkupdater.data.apkmirror.AppExistsResponse
import com.durcheins.apkupdater.BuildConfig
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApkMirrorService {

    @Headers(
        "User-Agent: APKUpdater-v" + BuildConfig.VERSION_NAME,
        "Authorization: Basic YXBpLWFwa3VwZGF0ZXI6cm01cmNmcnVVakt5MDRzTXB5TVBKWFc4",
        "Content-Type: application/json"
    )
    @POST("/wp-json/apkm/v1/app_exists/")
	suspend fun appExists(
        @Body request: AppExistsRequest
    ): AppExistsResponse

}
