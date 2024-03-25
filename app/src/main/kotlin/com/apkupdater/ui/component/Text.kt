package com.apkupdater.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.apkupdater.prefs.Prefs
import org.koin.androidx.compose.get


@Composable
fun TextBubble(code: Long, modifier: Modifier = Modifier) = TextBubble(
    if (code == 0L) "?" else code.toString(),
    modifier
)

@Composable
fun TextBubble(text: String, modifier: Modifier = Modifier) = Text(
    modifier = Modifier
        .padding(4.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp)
        )
        .then(modifier),
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
fun MediumText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) = Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = maxLines,
    modifier = modifier,
    overflow = TextOverflow.Ellipsis
)

@Composable
fun MediumTitle(text: String, modifier: Modifier = Modifier) = Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier
)

@Composable
fun LargeTitle(text: String, modifier: Modifier = Modifier) = Text(
    text = text,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    maxLines = 1,
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
    content: @Composable RowScope.() -> Unit
) {
    val state = rememberScrollState()
    val inner = remember { mutableStateOf(IntSize.Zero) }
    val outer = remember { mutableStateOf(IntSize.Zero) }

    if (get<Prefs>().playTextAnimations.get()) {
        val effect: suspend (ScrollState) -> Unit = {
            state.scrollTo(0)
            val scroll = (inner.value.width - outer.value.width)
            if (scroll > 0) {
                while(true) {
                    state.animateScrollTo(
                        scroll,
                        tween(delayMillis = 1000, durationMillis = scroll * 10, easing = LinearEasing)
                    )
                    state.animateScrollTo(
                        0,
                        tween(delayMillis = 1000, durationMillis = scroll * 10, easing = LinearEasing)
                    )
                }

            }
        }
        LaunchedEffect(outer.value) { effect(state) }
    }
    
    Row(Modifier.onSizeChanged { outer.value = it }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(state)
                .onSizeChanged { inner.value = it }
                .then(modifier),
            content = content
        )
    }
}

@Composable
fun BadgeText(number: String) {
    if (number.isNotEmpty()) {
        Badge {
            Text(number)
        }
    }
}

@Composable
fun ExpandingAnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    minLines: Int = 2,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Text(
        text =  if (text.text.last() == '\n') text.subSequence(0, text.length - 1) else text,
        maxLines = if (isExpanded) Int.MAX_VALUE else minLines,
        style = style,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clickable(true) { isExpanded = !isExpanded }
            .animateContentSize(),
    )
}
