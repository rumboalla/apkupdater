package com.apkupdater.data.apkpure


data class AppInfoForUpdate(
    val package_name: String,
    val version_code: Long,
    val is_system: Boolean = false,
    val version_id: String = "",
    val cached_size: Int = -1
)
