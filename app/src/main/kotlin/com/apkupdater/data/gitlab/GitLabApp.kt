package com.apkupdater.data.gitlab

import kotlinx.serialization.Serializable

@Serializable
data class GitLabApp(
    val packageName: String,
    val user: String,
    val repo: String
)

val GitLabApps = listOf(
    GitLabApp("com.aurora.store", "AuroraOSS", "AuroraStore")
)
