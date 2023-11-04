package com.apkupdater.data.gitlab

data class GitLabApp(
    val packageName: String,
    val user: String,
    val repo: String
)

val GitLabApps = listOf(
    GitLabApp("com.aurora.store", "AuroraOSS", "AuroraStore")
)
