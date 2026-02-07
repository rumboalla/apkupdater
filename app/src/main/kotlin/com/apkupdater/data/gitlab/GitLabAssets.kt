package com.apkupdater.data.gitlab

import kotlinx.serialization.Serializable


@Serializable
data class GitLabAssets(
    val sources: List<GitLabAsset>,
    val links: List<GitLabLink>
)
