package com.apkupdater.data.codeberg

data class CodeBergApp(
    val packageName: String,
    val user: String,
    val repo: String,
)

val CodeBergApps = listOf(
    CodeBergApp("eu.kanade.fabsemanga.psyduck", "fabseman", "fabsemanga"),
    CodeBergApp("com.draco.buoy", "s1m", "savertuner")
)
