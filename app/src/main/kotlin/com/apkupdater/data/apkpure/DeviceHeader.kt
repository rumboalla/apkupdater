package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class DeviceHeader(
    val device_info: DeviceInfo = DeviceInfo()
)
