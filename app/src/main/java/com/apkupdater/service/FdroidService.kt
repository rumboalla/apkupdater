package com.apkupdater.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface FdroidService {

    @Streaming
    @GET("index-v1.jar")
    suspend fun getJar(): ResponseBody

}
