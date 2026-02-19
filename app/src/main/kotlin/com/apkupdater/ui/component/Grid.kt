package com.apkupdater.ui.component

import android.content.res.Configuration
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyGridScope
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import com.apkupdater.prefs.Prefs
import com.apkupdater.ui.theme.statusBarColor
import org.koin.androidx.compose.get

private const val SHIMMER_ANIMATION_DURATION_MS = 1000
private const val SHIMMER_HIGH_ALPHA = 0.9f
private const val SHIMMER_LOW_ALPHA = 0.3f

@Composable
fun LoadingGrid() {
    if (get<Prefs>().androidTvUi.get()) {
        TvShimmeringGrid()
    } else {
        ShimmeringGrid()
    }
}

@Composable
fun ShimmeringGrid() = InstalledGrid(false) {
    items(16) {
        SkeletonItem()
    }
}

@Composable
fun TvShimmeringGrid() = TvInstalledGrid(false) {
    items(16) {
        SkeletonItem()
    }
}

@Composable
fun SkeletonItem() {
    val transition = rememberInfiniteTransition("SkeletonItemTransition")

    Column {
        // Image placeholder
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer(transition)
        )
        // Text placeholder
        Column(Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)) {
            Box(
                Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(transition)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(transition)
            )
        }
    }
}

private fun Modifier.shimmer(transition: InfiniteTransition): Modifier = composed {
    val color = MaterialTheme.colorScheme.statusBarColor()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(SHIMMER_ANIMATION_DURATION_MS)),
        label = "shimmer"
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = SHIMMER_HIGH_ALPHA),
                color.copy(alpha = SHIMMER_LOW_ALPHA),
                color.copy(alpha = SHIMMER_HIGH_ALPHA)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun EmptyGrid(
    text: String = ""
) = Box(Modifier.fillMaxSize()) {
    if (text.isNotEmpty()) {
        MediumTitle(text, Modifier.align(Alignment.Center))
    }
    LazyColumn(Modifier.fillMaxSize()) {}
}

@Composable
fun InstalledGrid(
    scroll: Boolean = true,
    content: LazyGridScope.() -> Unit
) = LazyVerticalGrid(
    columns =  GridCells.Fixed(getNumColumns(LocalConfiguration.current.orientation)),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content,
    userScrollEnabled = scroll,
    modifier = Modifier.fillMaxSize()
)

@Composable
fun TvInstalledGrid(scroll: Boolean = true, content: TvLazyGridScope.() -> Unit) = TvLazyVerticalGrid(
    columns = TvGridCells.Fixed(getTvNumColumns()),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content,
    userScrollEnabled = scroll,
    modifier = Modifier.fillMaxSize()
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
fun getTvNumColumns(): Int {
    return if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
        1
    else
        2
}
