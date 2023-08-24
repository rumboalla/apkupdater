package com.apkupdater.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.apkupdater.ui.theme.statusBarColor


fun Modifier.shimmering(enabled: Boolean): Modifier = composed {
    if (enabled) {
        var size by remember { mutableStateOf(IntSize.Zero) }
        val transition = rememberInfiniteTransition("shimmering")
        val color = MaterialTheme.colorScheme.statusBarColor()
        val startOffsetX by transition.animateFloat(
            label = "shimmering",
            initialValue = -2 * size.width.toFloat(),
            targetValue = 2 * size.width.toFloat(),
            animationSpec = infiniteRepeatable(animation = tween(1000))
        )
        background(
            brush = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.9f),
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.9f)
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        ).onGloballyPositioned {
            size = it.size
        }
    } else {
        Modifier
    }
}
