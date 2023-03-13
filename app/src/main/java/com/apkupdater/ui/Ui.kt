package com.apkupdater.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkupdater.model.ui.AppInstalled
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun BodyText(text: String) = Text(
    text,
    style = MaterialTheme.typography.body2,
    maxLines = 1,
    color = MaterialTheme.colors.onBackground
)

@Composable
fun TitleText(text: String) = Text(
    text,
    style = MaterialTheme.typography.h6,
    maxLines = 1,
    color = MaterialTheme.colors.onBackground
)

@Composable
fun ActionText(text: String) = Text(
    text,
    style = MaterialTheme.typography.body1,
    fontWeight = FontWeight.Bold,
    maxLines = 1,
    color = MaterialTheme.colors.primary
)

@Composable
fun SourceIcon(icon: Int) = Icon(
    painter = painterResource(icon),
    contentDescription = "Source Icon",
    modifier = Modifier.size(24.dp),
    tint = MaterialTheme.colors.primary
)

@Composable
fun CustomCard(alpha: Float, content: @Composable () -> Unit) = Card(
    content = content,
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, MaterialTheme.colors.primary),
    backgroundColor = Color.Transparent,
    modifier = Modifier
        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
        .fillMaxSize()
        .alpha(alpha)
)

data class Action(val name: String, val callback: () -> Unit)

@Composable
fun ActionRow(actionOne: Action? = null, actionTwo: Action? = null, icon: Int? = null) = Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically
) {
    icon?.let {
        SourceIcon(it)
        Spacer(modifier = Modifier.weight(1f))
    }
    actionTwo?.let {
        TextButton(onClick = it.callback) { ActionText(it.name) }
    }
    actionOne?.let {
        TextButton(onClick = it.callback) { ActionText(it.name) }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AppInfo(app: AppInstalled) = Row {
    GlideImage(modifier = Modifier.size(64.dp), model = app.iconUri, contentDescription = "App Icon")
    Spacer(modifier = Modifier.padding(8.dp))
    Column {
        TitleText(app.name)
        BodyText(app.packageName)
        BodyText(app.version)
    }
}
