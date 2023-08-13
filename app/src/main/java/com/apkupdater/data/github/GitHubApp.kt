package com.apkupdater.data.github

data class GitHubApp(
    val packageName: String,
    val user: String,
    val repo: String
)

val GitHubApps = listOf(
    GitHubApp("org.schabi.newpipe", "TeamNewPipe", "NewPipe"),
    GitHubApp("eu.faircode.netguard", "M66B", "NetGuard")
)
