package com.durcheins.apkupdater.data.ui

import com.durcheins.apkupdater.R


data class Source(
    val name: String,
    val resourceId: Int
)

val ApkMirrorSource = Source("ApkMirror", R.drawable.ic_apkmirror)
val GitHubSource = Source("GitHub", R.drawable.ic_github)
val PlaySource = Source("Play", R.drawable.ic_play)
