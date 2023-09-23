package com.apkupdater.data.ui

import com.apkupdater.R


data class Source(
    val name: String,
    val resourceId: Int
)

val ApkMirrorSource = Source("ApkMirror", R.drawable.ic_apkmirror)
val GitHubSource = Source("GitHub", R.drawable.ic_github)
val FdroidSource = Source("F-Droid (Main)", R.drawable.ic_fdroid)
val IzzySource = Source("F-Droid (Izzy)", R.drawable.ic_izzy)
val AptoideSource = Source("Aptoide", R.drawable.ic_aptoide)
val ApkPureSource = Source("ApkPure", R.drawable.ic_apkpure)
val GitLabSource = Source("GitLab", R.drawable.ic_gitlab)
