package com.apkupdater.data.gitlab

import kotlinx.serialization.Serializable


@Serializable
data class GitLabRelease(
    val tag_name: String,
    val description: String,
    val assets: GitLabAssets,
    val author: GitLabAuthor
)
