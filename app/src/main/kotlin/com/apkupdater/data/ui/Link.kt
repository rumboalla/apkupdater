package com.apkupdater.data.ui


sealed class Link {
    data object Empty: Link()
    data class Url(val link: String): Link()
    data class Xapk(val link: String): Link()
    data class Play(val getInstallFiles: () -> List<String>): Link()
}
