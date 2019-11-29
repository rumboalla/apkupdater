package com.apkupdater.model.aptoide

data class ListSearchAppsResponse(val datalist: DataList, val info: Any, val errors: Any)

data class DataList(val list: List<App> = emptyList())