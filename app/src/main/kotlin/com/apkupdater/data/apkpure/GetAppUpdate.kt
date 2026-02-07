package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable

import okhttp3.internal.toHexString
import kotlin.random.Random


@Serializable
data class GetAppUpdate(
    val app_info_for_update: List<AppInfoForUpdate> = emptyList(),
    val android_id: String = Random.nextLong().toHexString(),
    val application_id: String = "com.apkpure.aegon",
    val cached_size: Long = -1
)
