package com.apkupdater.ui.component

 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.material3.CircularProgressIndicator
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
fun AppImage(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Box {
	LoadingImageApp(app.packageName)
	TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	IgnoreIcon(
		app.ignored,
		{ onIgnore(app.packageName) },
		Modifier
			.align(Alignment.TopEnd)
			.padding(4.dp)
	)
}

@Composable
fun UpdateImage(app: AppUpdate, onInstall: (String) -> Unit = {}) = Box {
	LoadingImageApp(app.packageName)
	TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	InstallProgressIcon(app.isInstalling) { onInstall(app.link) }
	SourceIcon(
		app.source,
		Modifier
			.align(Alignment.TopStart)
			.padding(4.dp)
			.size(32.dp)
	)
}


@Composable
fun SearchImage(app: AppUpdate, onInstall: (String) -> Unit = {}) = Box {
	LoadingImage(app.iconUri)
	if (app.versionCode != 0L)
		TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	InstallProgressIcon(app.isInstalling) { onInstall(app.link) }
	SourceIcon(
		app.source,
		Modifier
			.align(Alignment.TopStart)
			.padding(4.dp)
			.size(32.dp)
	)
}

@Composable
fun InstalledItem(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Column(
	modifier = Modifier.alpha(if (app.ignored) 0.5f else 1f)
) {
	AppImage(app, onIgnore)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		MediumTitle(app.name)
	}
}

@Composable
fun UpdateItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Column {
	UpdateImage(app, onInstall)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		MediumTitle(app.name.ifEmpty { LocalContext.current.getAppName(app.packageName) })
	}
}

@Composable
fun SearchItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Column {
	SearchImage(app, onInstall)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		MediumTitle(app.name)
	}
}

@Composable
fun DefaultErrorScreen() = Box(Modifier.fillMaxSize()) {
	HugeText(
		stringResource(R.string.something_went_wrong),
		Modifier.align(Alignment.Center),
		2
	)
}

@Composable
fun DefaultLoadingScreen() = Box(Modifier.fillMaxSize()) {
	CircularProgressIndicator(Modifier.align(Alignment.Center))
}
