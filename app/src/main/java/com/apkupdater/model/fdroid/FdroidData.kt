package com.apkupdater.model.fdroid

data class FdroidData(val packages: Map<String, List<FdroidPackage>>, val apps: List<FdroidApp>, val arches: List<String> = emptyList())

data class FdroidPackage(val apkName: String = "", val versionCode: Int = 0, val versionName: String = "")

data class FdroidApp(val description: String = "", val packageName: String = "", val name: String = "", val icon: String = "", val suggestedVersionCode: String = "")