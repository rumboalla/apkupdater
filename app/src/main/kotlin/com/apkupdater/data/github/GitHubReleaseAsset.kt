package com.apkupdater.data.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseAsset(
    val size: Long,
    val browser_download_url: String
)
