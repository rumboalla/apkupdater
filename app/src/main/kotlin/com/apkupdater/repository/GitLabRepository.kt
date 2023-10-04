package com.apkupdater.repository

import com.apkupdater.data.gitlab.GitLabApps
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.prefs.Prefs
import com.apkupdater.service.GitLabService
import kotlinx.coroutines.flow.flow


class GitLabRepository(
    private val service: GitLabService,
    private val prefs: Prefs
) {

    suspend fun updates(apps: List<AppInstalled>) = flow {

        GitLabApps.forEach { app ->
            apps.find { it.packageName == app.packageName }?.let {
                //checks.add(checkApp(apps, app.user, app.repo, app.packageName, it.version, app.extra))
            }
        }
        emit(0)
    }

    private suspend fun checkApp() {}

}
