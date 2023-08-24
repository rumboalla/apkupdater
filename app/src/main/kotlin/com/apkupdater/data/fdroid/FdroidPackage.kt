package com.apkupdater.data.fdroid

data class FdroidPackage(
    val apkName: String = "",
    val versionCode: Long = 0,
    val versionName: String = "",
    val minSdkVersion: Int = 0,
    val nativecode: List<String> = emptyList(),
    val hash: String = "",
    val hashType: String = ""
)
