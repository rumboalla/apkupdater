package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class GetAppUpdateResponse(
    val retcode: Int,
    val app_update_response: List<AppUpdateResponse>
)
