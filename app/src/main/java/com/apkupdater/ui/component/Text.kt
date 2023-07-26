package com.apkupdater.ui.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp


@Composable
fun TextBubble(text: String, modifier: Modifier = Modifier) = Text(
    modifier = Modifier
        .padding(4.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp)
        ).then(modifier),
    text = "  $text  "
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
        modifier = Modifier.fillMaxWidth().horizontalScroll(state).then(modifier),
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
