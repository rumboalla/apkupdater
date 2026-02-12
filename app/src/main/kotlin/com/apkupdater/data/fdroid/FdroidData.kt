package com.apkupdater.data.fdroid

import kotlinx.serialization.Serializable

@Serializable
data class FdroidData(
	val packages: Map<String, List<FdroidPackage>> = emptyMap(),
	val apps: List<FdroidApp> = emptyList()
)
