package com.apkupdater.data.github


data class GitHubRelease(
    val name: String,
    val prerelease: Boolean,
    val assets: List<GitHubReleaseAsset>,
    val tag_name: String,
    val author: GitHubAuthor,
    val body: String = ""
)
