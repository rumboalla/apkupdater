package com.apkupdater.data.ui

import com.apkupdater.R


data class Source(
    val name: String,
    val resourceId: Int
)

val ApkMirrorSource = Source("ApkMirror", R.drawable.ic_apkmirror)
val GitHubSource = Source("GitHub", R.drawable.ic_github)
