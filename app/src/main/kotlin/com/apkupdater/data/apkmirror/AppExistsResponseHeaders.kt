package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class AppExistsResponseHeaders(@SerialName("Allow") val allow: String? = null)
