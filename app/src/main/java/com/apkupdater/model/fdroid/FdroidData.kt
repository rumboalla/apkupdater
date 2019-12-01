package com.apkupdater.model.fdroid

data class FdroidData(val packages: Map<String, List<FdroidPackage>>)

data class FdroidPackage(val apkName: String = "", val versionCode: Int = 0, val versionName: String = "")