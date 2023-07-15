package com.apkupdater.ui.component

 import android.content.res.Configuration
 import android.net.Uri
 import androidx.compose.animation.core.AnimationSpec
 import androidx.compose.animation.core.FastOutSlowInEasing
 import androidx.compose.animation.core.RepeatMode
 import androidx.compose.animation.core.infiniteRepeatable
 import androidx.compose.animation.core.tween
 import androidx.compose.foundation.ScrollState
 import androidx.compose.foundation.background
 import androidx.compose.foundation.horizontalScroll
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.PaddingValues
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.RowScope
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.grid.GridCells
 import androidx.compose.foundation.lazy.grid.LazyGridScope
 import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
 import androidx.compose.foundation.rememberScrollState
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material3.Badge
 import androidx.compose.material3.CircularProgressIndicator
 import androidx.compose.material3.ExperimentalMaterial3Api
 import androidx.compose.material3.Icon
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.PlainTooltipBox
 import androidx.compose.material3.Slider
 import androidx.compose.material3.Switch
 import androidx.compose.material3.Text
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.remember
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.alpha
 import androidx.compose.ui.draw.clip
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.layout.ContentScale
 import androidx.compose.ui.platform.LocalConfiguration
 import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.res.painterResource
 import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.TextStyle
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.text.style.TextOverflow
 import androidx.compose.ui.unit.Dp
 import androidx.compose.ui.unit.TextUnit
 import androidx.compose.ui.unit.TextUnitType
 import androidx.compose.ui.unit.dp
 import coil.compose.AsyncImage
 import coil.request.ImageRequest
 import com.apkupdater.R
 import com.apkupdater.data.ui.AppInstalled
 import com.apkupdater.data.ui.AppUpdate
 import com.apkupdater.prefs.Prefs
 import com.apkupdater.util.clickableNoRipple
 import com.apkupdater.util.getAppIcon
 import org.koin.androidx.compose.get


@Composable
fun TextBubble(text: String, modifier: Modifier = Modifier) = Text(
	modifier = Modifier
		.padding(4.dp)
		.background(
			color = Color(
				MaterialTheme.colorScheme.primaryContainer.red,
				MaterialTheme.colorScheme.primaryContainer.green,
				MaterialTheme.colorScheme.primaryContainer.blue,
				0.7f
			),
			shape = RoundedCornerShape(16.dp)
		)
		.then(modifier),
	text = "  $text  "
)

@Composable
fun IgnoreIcon(ignored: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) = Icon(
	painter = painterResource(
		id = if(ignored) R.drawable.ic_visible_off else R.drawable.ic_visible
	),
	contentDescription = stringResource(R.string.ignored_cd),
	modifier = Modifier
		.clickableNoRipple(onClick)
		.then(modifier)
)

@Composable
fun InstallIcon(onClick: () -> Unit, modifier: Modifier = Modifier) = Icon(
	painter = painterResource(R.drawable.ic_install),
	contentDescription = stringResource(R.string.install_cd),
	modifier = Modifier
		.clickableNoRipple(onClick)
		.then(modifier)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIcon(
	text: String,
	modifier: Modifier = Modifier
) = PlainTooltipBox(tooltip = { Text(text) }) {
	Icon(
		painter = painterResource(id = R.drawable.ic_refresh),
		contentDescription = text,
		modifier = Modifier
			.tooltipAnchor()
			.then(modifier)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludeSystemIcon(excludeSystem: Boolean) = PlainTooltipBox(
	tooltip = {
		val id = if (excludeSystem) R.string.include_system_apps else R.string.exclude_system_apps
		Text(stringResource(id))
	}
) {
	val icon = if (excludeSystem) R.drawable.ic_system_off else R.drawable.ic_system
	Icon(
		painterResource(icon),
		stringResource(R.string.exclude_system_apps),
		Modifier.tooltipAnchor()
	)
}

@Composable
fun LoadingImage(
	uri: Uri,
	height: Dp = 120.dp,
	color: Color = Color.Transparent
) = AsyncImage(
	model = ImageRequest
		.Builder(LocalContext.current)
		.data(uri)
		.crossfade(true)
		.build(),
	contentDescription = stringResource(R.string.app_cd),
	modifier = Modifier
		.fillMaxSize()
		.height(height)
		.padding(10.dp)
		.clip(RoundedCornerShape(8.dp))
		.background(color),
	contentScale = ContentScale.Fit
)

@Composable
fun LoadingImageApp(
	packageName: String,
	height: Dp = 120.dp,
	color: Color = Color.Transparent
) = AsyncImage(
	model = ImageRequest
		.Builder(LocalContext.current)
		.data(LocalContext.current.getAppIcon(packageName))
		.crossfade(true)
		.build(),
	contentDescription = stringResource(R.string.app_cd),
	modifier = Modifier
		.fillMaxSize()
		.height(height)
		.padding(10.dp)
		.clip(RoundedCornerShape(8.dp))
		.background(color),
	contentScale = ContentScale.Fit
)

@Composable
fun SmallText(text: String, modifier: Modifier = Modifier) = Text(
	text = text,
	style = MaterialTheme.typography.bodySmall,
	maxLines = 1,
	modifier = modifier,
	overflow = TextOverflow.Ellipsis
)

@Composable
fun TitleText(text: String, modifier: Modifier = Modifier) = Text(
	text = text,
	style = MaterialTheme.typography.titleMedium,
	fontWeight = FontWeight.Bold,
	maxLines = 2,
	overflow = TextOverflow.Ellipsis,
	modifier = modifier
)

@Composable
fun HugeText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) = Text(
	text = text,
	style = TextStyle(fontSize = TextUnit(36f, TextUnitType.Sp)),
	fontWeight = FontWeight.ExtraBold,
	maxLines = maxLines,
	overflow = TextOverflow.Ellipsis,
	modifier = modifier,
	textAlign = TextAlign.Center
)

@Composable
fun getNumColumns(orientation: Int): Int {
	val prefs = get<Prefs>()
	return if(orientation == Configuration.ORIENTATION_PORTRAIT)
		prefs.portraitColumns.get()
	else
		prefs.landscapeColumns.get()
}

@Composable
fun InstalledGrid(content: LazyGridScope.() -> Unit) = LazyVerticalGrid(
	columns =  GridCells.Fixed(getNumColumns(LocalConfiguration.current.orientation)),
	contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
	verticalArrangement = Arrangement.spacedBy(8.dp),
	horizontalArrangement = Arrangement.spacedBy(8.dp),
	content = content
)

@Composable
fun AppImage(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Box {
	LoadingImageApp(app.packageName)
	TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	IgnoreIcon(
		app.ignored,
		{ onIgnore(app.packageName) },
		Modifier.align(Alignment.TopEnd).padding(4.dp)
	)
}

@Composable
fun UpdateImage(app: AppUpdate, onInstall: (String) -> Unit = {}) = Box {
	LoadingImageApp(app.packageName)
	TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	InstallIcon(
		{ onInstall(app.link) },
		Modifier.align(Alignment.TopEnd).padding(4.dp)
	)
}

@Composable
fun SearchImage(app: AppUpdate, onInstall: (String) -> Unit = {}) = Box {
	LoadingImage(app.iconUri)
	if (app.versionCode != 0L)
		TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	InstallIcon(
		{ onInstall(app.link) },
		Modifier.align(Alignment.TopEnd).padding(4.dp)
	)
}

@Composable
fun InstalledItem(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Column(
	modifier = Modifier.alpha(if (app.ignored) 0.5f else 1f)
) {
	AppImage(app, onIgnore)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		TitleText(app.name)
	}
}

@Composable
fun UpdateItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Column {
	UpdateImage(app, onInstall)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		TitleText(app.name)
	}
}

@Composable
fun SearchItem(app: AppUpdate, onInstall: (String) -> Unit = {}) = Column {
	SearchImage(app, onInstall)
	Column(Modifier.padding(top = 4.dp)) {
		ScrollableText { SmallText(app.packageName) }
		TitleText(app.name)
	}
}

@Composable
fun ScrollableText(
	modifier: Modifier = Modifier,
	spec: AnimationSpec<Float> = infiniteRepeatable(
		animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
		repeatMode = RepeatMode.Reverse
	),
	effect: suspend (ScrollState) -> Unit = {
		it.scrollTo(0)
		it.animateScrollTo(it.maxValue, spec)
	},
	content: @Composable RowScope.() -> Unit
) {
	val state = rememberScrollState()
	LaunchedEffect(Unit) { effect(state) }
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(state)
			.then(modifier),
		content = content
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeText(number: String) {
	if (number.isNotEmpty()) {
		Badge {
			Text(number)
		}
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

@Composable
fun SliderSetting(
	getValue: () -> Float,
	setValue: (Float) -> Unit,
	text: String,
	valueRange: ClosedFloatingPointRange<Float>,
	steps: Int
) = Box(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp)) {
	var position by remember { mutableStateOf(getValue()) }
	Text(text, Modifier.align(Alignment.CenterStart))
	Row(Modifier.align(Alignment.CenterEnd)) {
		Text("${getValue().toInt()}", Modifier.align(Alignment.CenterVertically).padding(8.dp))
		Slider(
			value = position,
			valueRange = valueRange,
			steps = steps,
			onValueChange = {
				position = it
				setValue(it)
			},
			modifier = Modifier.width(150.dp)
		)
	}
}

@Composable
fun SwitchSetting(
	getValue: () -> Boolean,
	setValue: (Boolean) -> Unit,
	text: String
) = Box (Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp)) {
	var value by remember { mutableStateOf(getValue()) }
	Text(text, Modifier.align(Alignment.CenterStart))
	Switch(
		checked = value,
		onCheckedChange = {
			value = it
			setValue(it)
		},
		modifier = Modifier.align(Alignment.CenterEnd)
	)
}
