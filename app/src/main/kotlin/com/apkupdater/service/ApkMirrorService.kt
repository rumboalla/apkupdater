package com.apkupdater.service

import com.apkupdater.data.apkmirror.AppExistsRequest
import com.apkupdater.data.apkmirror.AppExistsResponse
import com.apkupdater.BuildConfig
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApkMirrorService {

    @Headers(
        "User-Agent: APKUpdater-v" + BuildConfig.VERSION_NAME,
        "Content-Type: application/json"
    )
    @POST("/wp-json/apkm/v1/app_exists/")
	suspend fun appExists(
        @Body request: AppExistsRequest
    ): AppExistsResponse

}
