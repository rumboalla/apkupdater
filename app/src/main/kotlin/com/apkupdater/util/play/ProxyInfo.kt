package com.apkupdater.util.play


data class ProxyInfo(
    var protocol: String,
    var host: String,
    var port: Int,
    var proxyUser: String?,
    var proxyPassword: String?
)
