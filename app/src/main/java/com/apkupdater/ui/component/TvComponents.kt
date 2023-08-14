package com.apkupdater.ui.component

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apkupdater.R
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.util.getAppName


@Composable
fun TvCommonItem(
    packageName: String,
    name: String,
    version: String,
    oldVersion: String?,
    versionCode: Long,
    oldVersionCode: Long?,
    uri: Uri? = null,
    single: Boolean = false
) = Row {
    if (uri == null) {
        LoadingImageApp(packageName, Modifier.height(100.dp).align(Alignment.CenterVertically).padding(top = 8.dp))
    } else {
        LoadingImage(uri, Modifier.height(100.dp).align(Alignment.CenterVertically).padding(top = 8.dp))
    }
    Column(Modifier.align(Alignment.CenterVertically).padding(start = 8.dp, end = 8.dp, top = 8.dp)) {
        LargeTitle(name.ifEmpty { LocalContext.current.getAppName(packageName) })
        MediumText(packageName)
        if (oldVersion != null && !single) {
            ScrollableText {
                MediumText("$oldVersion -> $version")
            }
        } else {
            MediumText(version)
        }
        val code = if (versionCode == 0L) "?" else versionCode.toString()
        if (oldVersionCode != null && !single) {
            MediumText("$oldVersionCode -> $code")
        } else {
            MediumText(code)
        }
    }
}

@Composable
fun TvInstallButton(
    app: AppUpdate,
    onInstall: (String) -> Unit
) = ElevatedButton(
    modifier = Modifier
        .padding(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
        .width(120.dp),
    onClick = { onInstall(app.packageName) }
) {
    if (app.isInstalling) {
        CircularProgressIndicator(Modifier.size(24.dp))
    } else {
        Text(stringResource(R.string.install_cd))
    }
}

@Composable
fun BoxScope.TvSourceIcon(app: AppUpdate) = SourceIcon(
    app.source,
    Modifier
        .align(Alignment.CenterStart)
        .padding(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
        .size(32.dp)
)

@Composable
fun TvInstalledItem(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Card(
    modifier = Modifier.alpha(if (app.ignored) 0.5f else 1f)
) {
    Column {
        TvCommonItem(app.packageName, app.name, app.version, null, app.versionCode, null)
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
            ElevatedButton(
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                onClick = { onIgnore(app.packageName) }
            ) {
                Text(stringResource(if (app.ignored) R.string.unignore_cd else R.string.ignore_cd))
            }
        }
    }
}

@Composable
fun TvUpdateItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Card {
    Column {
        TvCommonItem(app.packageName, app.name, app.version, app.oldVersion, app.versionCode, app.oldVersionCode)
        Box {
            TvSourceIcon(app)
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                TvInstallButton(app, onInstall)
            }
        }
    }
}

@Composable
fun TvSearchItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Card {
    Column {
        TvCommonItem(app.packageName, app.name, app.version, app.oldVersion, app.versionCode, app.oldVersionCode, app.iconUri, true)
        Box {
            TvSourceIcon(app)
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                TvInstallButton(app, onInstall)
            }
        }
    }
}
