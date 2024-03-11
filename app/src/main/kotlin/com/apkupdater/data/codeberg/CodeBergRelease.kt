package com.apkupdater.data.codeberg


data class CodeBergRelease(
    val name: String,
    val prerelease: Boolean,
    val assets: List<CodeBergReleaseAsset>,
    val tag_name: String,
    val author: CodeBergAuthor,
    val body: String = ""
)
