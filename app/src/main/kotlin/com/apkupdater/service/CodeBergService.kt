package com.apkupdater.service

import com.apkupdater.data.codeberg.CodeBergRelease
import retrofit2.http.GET
import retrofit2.http.Path

interface CodeBergService {

    @GET("/repos/{user}/{repo}/releases")
    suspend fun getReleases(
        @Path("user") user: String = "rumboalla",
        @Path("repo") repo: String = "apkupdater"
    ): List<CodeBergRelease>

}
