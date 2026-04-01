package com.durcheins.apkupdater.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.util.getAppIcon

@Composable
private fun BaseLoadingImage(
    request: ImageRequest,
    modifier: Modifier,
    color: Color = Color.Transparent
) = AsyncImage(
    model = request,
    contentDescription = stringResource(R.string.app_cd),
    modifier = modifier
        .padding(10.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(color),
    contentScale = ContentScale.Fit,
    error = painterResource(R.drawable.ic_root),
    placeholder = painterResource(R.drawable.ic_empty)
)

@Composable
fun LoadingImage(
    uri: Uri,
    modifier: Modifier = Modifier.height(120.dp).fillMaxSize(),
    color: Color = Color.Transparent
) = BaseLoadingImage(
    ImageRequest.Builder(LocalContext.current).data(uri).build(),
    modifier,
    color
)

@Composable
fun LoadingImageApp(
    packageName: String,
    modifier: Modifier = Modifier.height(120.dp).fillMaxSize(),
    color: Color = Color.Transparent
) = BaseLoadingImage(
    ImageRequest.Builder(LocalContext.current).data(LocalContext.current.getAppIcon(packageName)).build(),
    modifier,
    color
)
