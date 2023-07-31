package com.apkupdater.data.ui

import com.apkupdater.R


data class Source(
    val name: String,
    val resourceId: Int
)

val ApkMirrorSource = Source("ApkMirror", R.drawable.ic_apkmirror)
val GitHubSource = Source("GitHub", R.drawable.ic_github)
val FdroidSource = Source("F-Droid", R.drawable.ic_fdroid)
val AptoideSource = Source("Aptoide", R.drawable.ic_aptoide)
