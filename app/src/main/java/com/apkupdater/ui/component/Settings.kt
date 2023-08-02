package com.apkupdater.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apkupdater.R


@Composable
fun SliderSetting(
    getValue: () -> Float,
    setValue: (Float) -> Unit,
    text: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    @DrawableRes icon: Int
) = Row(
    Modifier
        .fillMaxWidth()
        .height(70.dp)
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
) {
    var position by remember { mutableFloatStateOf(getValue()) }
    Icon(painterResource(id = icon), text, Modifier.align(CenterVertically))
    Column(
        Modifier
            .padding(start = 16.dp)
            .fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            Text(text,
                Modifier
                    .align(CenterStart)
                    .padding(start = 8.dp))
            Text("${getValue().toInt()}",
                Modifier
                    .align(CenterEnd)
                    .padding(end = 8.dp))
        }
        Slider(
            value = position,
            valueRange = valueRange,
            steps = steps,
            onValueChange = {
                position = it
                setValue(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun SwitchSetting(
    getValue: () -> Boolean,
    setValue: (Boolean) -> Unit,
    text: String,
    @DrawableRes icon: Int = R.drawable.ic_system
) = Box (
    Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(horizontal = 16.dp)
) {
    var value by remember { mutableStateOf(getValue()) }
    Row(Modifier.align(CenterStart)) {
        Icon(
            painterResource(id = icon),
            text,
            Modifier.align(CenterVertically).padding(end = 16.dp).size(24.dp)
        )
        Text(text,  Modifier.align(CenterVertically))
    }
    Switch(
        checked = value,
        onCheckedChange = {
            value = it
            setValue(it)
        },
        modifier = Modifier.align(CenterEnd)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownSetting(
    text: String,
    options: List<String>,
    getValue: () -> Int,
    setValue: (Int) -> Unit,
    @DrawableRes icon: Int
) = Box(
    Modifier
        .padding(16.dp)
        .fillMaxWidth()) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[getValue()]) }

    Row(Modifier.align(CenterStart)) {
        Icon(
            painterResource(id = icon),
            text,
            Modifier.align(CenterVertically).padding(end = 16.dp).size(24.dp)
        )
        Text(text,  Modifier.align(CenterVertically))
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .align(CenterEnd)
            .width(150.dp)
    ) {
        CompositionLocalProvider(LocalTextInputService provides null) { // Disable Keyboard
            OutlinedTextField(
                readOnly = true,
                value = selectedOptionText,
                onValueChange = { setValue(options.indexOf(it)) },
                modifier = Modifier.menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        selectedOptionText = option
                        expanded = false
                    }
                )
            }
        }
    }
}
