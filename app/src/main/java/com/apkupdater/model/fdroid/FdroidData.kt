package com.apkupdater.model.fdroid

data class FdroidData(
	val packages: Map<String, List<FdroidPackage>>,
	val apps: List<FdroidApp>
)

data class FdroidPackage(
	val apkName: String = "",
	val versionCode: Int = 0,
	val versionName: String = "",
	val minSdkVersion: Int = 0,
	val nativecode: List<String> = emptyList()
)

data class FdroidApp(
	val description: String = "",
	val packageName: String = "",
	val name: String = "",
	val icon: String = "",
	val suggestedVersionCode: String = "",
	val suggestedVersionName: String = "",
	val added: Long = 0L,
	val lastUpdated: Long = 0L
)