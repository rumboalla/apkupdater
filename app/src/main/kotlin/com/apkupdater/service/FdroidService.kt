package com.apkupdater.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FdroidService {

    @Streaming
    @GET
    suspend fun getJar(@Url url: String): ResponseBody

}
