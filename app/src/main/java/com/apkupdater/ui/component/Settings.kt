package com.apkupdater.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SliderSetting(
    getValue: () -> Float,
    setValue: (Float) -> Unit,
    text: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) = Box(
    Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp)) {
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
) = Box (
    Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp)) {
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
