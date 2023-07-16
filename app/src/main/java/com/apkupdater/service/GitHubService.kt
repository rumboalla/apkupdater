package com.apkupdater.service

import com.apkupdater.data.github.GitHubRelease
import retrofit2.http.GET

interface GitHubService {

    @GET("/repos/rumboalla/apkupdater/releases")
    suspend fun getReleases(): List<GitHubRelease>

}
