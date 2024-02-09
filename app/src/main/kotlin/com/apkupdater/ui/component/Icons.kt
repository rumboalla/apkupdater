package com.apkupdater.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.TooltipBoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apkupdater.R
import com.apkupdater.data.ui.Source
import com.apkupdater.util.clickableNoRipple

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
) = PlainTooltipBox(
    tooltip = { Text(stringResource(string)) },
    content = {
        Icon(
            painterResource(icon),
            stringResource(contentDescription),
            Modifier.tooltipTrigger()
        )
    }
)

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
fun BoxScope.InstallProgressIcon(
    isInstalling: Boolean,
    onClick: () -> Unit
) {
    if(isInstalling) {
        CircularProgressIndicator(
            Modifier.align(Alignment.TopEnd).size(30.dp).padding(4.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
    else {
        InstallIcon(
            { onClick() },
            Modifier.align(Alignment.TopEnd).padding(4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIcon(
    text: String,
    modifier: Modifier = Modifier
) = PlainTooltipBox(
    tooltip = { Text(text) },
    content = {
        Icon(
            painter = painterResource(id = R.drawable.ic_refresh),
            contentDescription = text,
            modifier = Modifier.tooltipTrigger().then(modifier)
        )
    }
)
