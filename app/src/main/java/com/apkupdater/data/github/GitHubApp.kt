package com.apkupdater.data.github

data class GitHubApp(
    val packageName: String,
    val user: String,
    val repo: String
)

val GitHubApps = listOf(
    GitHubApp("com.apkupdater", "rumboalla", "apkupdater"),
    GitHubApp("org.schabi.newpipe", "TeamNewPipe", "NewPipe"),
    GitHubApp("eu.faircode.netguard", "M66B", "NetGuard"),
    GitHubApp("org.adaway", "AdAway", "AdAway"),
    GitHubApp("com.duckduckgo.mobile.android", "duckduckgo", "Android"),
    GitHubApp("com.foobnix.pro.pdf.reader", "foobnix", "LibreraReader"),
    GitHubApp("com.kunzisoft.keepass.free", "Kunzisoft", "KeePassDX"),
    GitHubApp("dev.imranr.obtainium", "ImranR98", "Obtainium")
)
