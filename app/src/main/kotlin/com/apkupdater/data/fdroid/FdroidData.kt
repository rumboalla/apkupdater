package com.apkupdater.data.fdroid

data class FdroidData(
	val packages: Map<String, List<FdroidPackage>> = emptyMap(),
	val apps: List<FdroidApp> = emptyList()
)
