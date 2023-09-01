package com.apkupdater.service

import com.apkupdater.data.apkpure.GetAppUpdate
import com.apkupdater.data.apkpure.GetAppUpdateResponse
import com.apkupdater.data.apkpure.SearchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface ApkPureService {

    @Headers(
        "content-type: application/json",
        "ual-access-businessid: projecta"
    )
    @POST("v3/get_app_update")
    suspend fun getAppUpdate(
        @Header("ual-access-projecta") header: String,
        @Body request: GetAppUpdate
    ): GetAppUpdateResponse

    @Headers(
        "ual-access-businessid: projecta",
    )
    @GET("v3/search_query_new")
    suspend fun search(
        @Header("ual-access-projecta") header: String,
        @Query("key") key: String
    ): SearchResponse

}
