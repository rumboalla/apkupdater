package com.apkupdater.data.ui

import com.aurora.gplayapi.data.models.File


sealed class Link {
    data object Empty: Link()
    data class Url(val link: String, val size: Long = 0L): Link()
    data class Xapk(val link: String): Link()
    data class Play(val getInstallFiles: () -> List<File>): Link()
}
