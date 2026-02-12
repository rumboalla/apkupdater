package com.apkupdater.data.gitlab

import kotlinx.serialization.Serializable


@Serializable
data class GitLabAsset(val format: String, val url: String)
