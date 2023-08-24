package com.apkupdater.data.fdroid

data class FdroidApp(
    //val description: String = "",
    val packageName: String = "",
    val name: String = "",
    val icon: String = "",
    val suggestedVersionCode: String = "",
    val suggestedVersionName: String = "",
    val added: Long = 0L,
    val lastUpdated: Long = 0L,
    val allowedAPKSigningKeys: List<String> = emptyList(),
    val localized: Map<String, FdroidLocalized> = emptyMap()
)
