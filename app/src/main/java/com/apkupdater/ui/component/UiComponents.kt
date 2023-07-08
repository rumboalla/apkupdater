package com.apkupdater.ui.component

import android.content.res.Configuration
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.apkupdater.data.AppInstalled
import com.apkupdater.util.clickableNoRipple
import com.apkupdater.util.getAppIcon


@Composable
fun TextBubble(text: String, modifier: Modifier = Modifier) = Text(
	modifier = Modifier
		.padding(4.dp)
		.background(
			color = Color(0, 0, 0, 180),
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
fun RefreshIcon(modifier: Modifier = Modifier, onClick: () -> Unit = {}) = Icon(
	painter = painterResource(id = R.drawable.ic_refresh),
	contentDescription = stringResource(R.string.refresh_cd),
	modifier = Modifier
		.clickable(MutableInteractionSource(), null) { onClick() }
		.then(modifier)
)

@Composable
fun ExcludeSystemIcon(excludeSystem: Boolean) {
	val icon = if (excludeSystem) R.drawable.ic_system_off else R.drawable.ic_system
	Icon(painterResource(icon), stringResource(R.string.exclude_system_cd))
}

@Composable
fun LoadingImage(
	url: String,
	height: Dp = 195.dp,
	color: Color = Color.Transparent
) = AsyncImage(
	model = ImageRequest.Builder(LocalContext.current).data(LocalContext.current.getAppIcon(url)).crossfade(true).build(),
	contentDescription = stringResource(R.string.app_cd),
	modifier = Modifier
		.fillMaxSize()
		.height(height)
		.padding(10.dp)
		.clip(RoundedCornerShape(8.dp))
		.background(color),
	contentScale = ContentScale.Fit,
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
fun getNumCells(orientation: Int) = if(orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4

@Composable
fun InstalledGrid(content: LazyGridScope.() -> Unit) = LazyVerticalGrid(
	columns =  GridCells.Fixed(getNumCells(LocalConfiguration.current.orientation)),
	contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
	verticalArrangement = Arrangement.spacedBy(8.dp),
	horizontalArrangement = Arrangement.spacedBy(8.dp),
	content = content
)

@Composable
fun AppImage(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Box {
	LoadingImage(app.packageName)
	TextBubble(app.versionCode.toString(), Modifier.align(Alignment.BottomStart))
	IgnoreIcon(
		app.ignored,
		{  onIgnore(app.packageName) },
		Modifier.align(Alignment.TopEnd).padding(4.dp)
	)
}

@Composable
fun AppItem(app: AppInstalled, onIgnore: (String) -> Unit = {}) = Column(
	modifier = Modifier.alpha(if (app.ignored) 0.5f else 1f)
) {
	AppImage(app, onIgnore)
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
