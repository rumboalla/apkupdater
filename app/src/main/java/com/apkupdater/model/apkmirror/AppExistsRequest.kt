package com.apkupdater.model.apkmirror

data class AppExistsRequest(var pnames: List<String>, var exclude: List<String> = listOf("alpha", "beta"))
