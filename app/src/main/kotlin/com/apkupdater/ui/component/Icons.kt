package com.apkupdater.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkupdater.R
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Link
import com.apkupdater.data.ui.Source
import com.apkupdater.util.clickableNoRipple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludeIcon(
    exclude: Boolean,
    @StringRes excludeString: Int,
    @StringRes includeString: Int,
    @DrawableRes excludeIcon: Int,
    @DrawableRes includeIcon: Int,
    @DrawableRes icon: Int = if (exclude) excludeIcon else includeIcon,
    @StringRes string: Int = if (exclude) includeString else excludeString,
    @StringRes contentDescription: Int = if (exclude) excludeString else includeString,
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    state = rememberTooltipState(),
    tooltip = { PlainTooltip { Text(stringResource(string)) } },
) {
    Icon(painterResource(icon), stringResource(contentDescription))
}

@Composable
fun ExcludeSystemIcon(exclude: Boolean) = ExcludeIcon(
    exclude = exclude,
    excludeString = R.string.exclude_system_apps,
    includeString = R.string.include_system_apps,
    excludeIcon = R.drawable.ic_system_off,
    includeIcon = R.drawable.ic_system
)

@Composable
fun ExcludeAppStoreIcon(exclude: Boolean) = ExcludeIcon(
    exclude = exclude,
    excludeString = R.string.exclude_app_store,
    includeString = R.string.include_app_store,
    excludeIcon = R.drawable.ic_appstore_off,
    includeIcon = R.drawable.ic_appstore
)

@Composable
fun ExcludeDisabledIcon(exclude: Boolean) = ExcludeIcon(
    exclude = exclude,
    excludeString = R.string.exclude_disabled_apps,
    includeString = R.string.include_disabled_apps,
    excludeIcon = R.drawable.ic_disabled_off,
    includeIcon = R.drawable.ic_disabled
)

@Composable
fun SourceIcon(source: Source, modifier: Modifier = Modifier) = Icon(
    painterResource(id = source.resourceId),
    source.name,
    modifier
)

@Composable
fun IgnoreIcon(ignored: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) = Icon(
    painter = painterResource(
        id = if(ignored) R.drawable.ic_visible_off else R.drawable.ic_visible
    ),
    contentDescription = stringResource(if (ignored) R.string.unignore_cd else R.string.ignore_cd),
    modifier = Modifier.clickableNoRipple(onClick).then(modifier)
)

@Composable
fun InstallIcon(onClick: () -> Unit, modifier: Modifier = Modifier) = Icon(
    painter = painterResource(R.drawable.ic_install),
    contentDescription = stringResource(R.string.install_cd),
    modifier = Modifier.clickableNoRipple(onClick).then(modifier)
)

@Composable
fun LinkIcon(onClick: () -> Unit, modifier: Modifier = Modifier) = Icon(
    imageVector = Icons.Default.Info,
    contentDescription = "Direct Link",
    modifier = Modifier.clickableNoRipple(onClick).then(modifier)
)

@Composable
fun BoxScope.InstallProgressIcon(
    app: AppUpdate,
    onClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    if (!(app.isInstalling) && app.error == null) {
        Row(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            LinkIcon(
                onClick = {
                    when (val link = app.link) {
                        is Link.Url -> uriHandler.openUri(link.link)
                        is Link.Xapk -> uriHandler.openUri(link.link)
                        is Link.Play -> {
                            // Play Store links expire quickly and require special headers.
                            // We can't easily open them in a browser, so we'll just not show an action or 
                            // maybe show a toast in the future.
                        }
                        else -> {}
                    }
                },
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            if (!app.isPersistent) {
                InstallIcon(
                    { onClick() },
                    Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.persistent_app_warning),
                    modifier = Modifier
                        .size(24.dp)
                        .clickableNoRipple { onClick() },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    } else if (app.isInstalling) {
        val progress = if (app.total > 0) app.progress.toFloat() / app.total.toFloat() else 0f
        val animatedProgress by animateFloatAsState(targetValue = progress)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = app.status,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (app.total > 0) animatedProgress else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    trackColor = Color.White.copy(alpha = 0.3f),
                    color = MaterialTheme.colorScheme.primary
                )
                if (app.total > 0) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    } else {
        app.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                    .clickableNoRipple { onClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_visible_off), // Using visible_off as a generic alert/error icon
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "RETRY",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIcon(
    text: String,
    modifier: Modifier = Modifier
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    state = rememberTooltipState(),
    tooltip = { PlainTooltip { Text(text) } }
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_refresh),
        contentDescription = text,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadIcon(
    text: String,
    modifier: Modifier = Modifier
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    state = rememberTooltipState(),
    tooltip = { PlainTooltip { Text(text) } }
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_download),
        contentDescription = text,
        modifier = modifier
    )
}
