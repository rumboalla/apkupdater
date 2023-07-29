package com.apkupdater.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownSetting(
    text: String,
    options: List<String>,
    getValue: () -> Int,
    setValue: (Int) -> Unit
) = Box(Modifier.padding(16.dp).fillMaxWidth()) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[getValue()]) }

    Text(text, modifier = Modifier.align(Alignment.CenterStart))
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.align(Alignment.CenterEnd).width(150.dp)
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { setValue(options.indexOf(it)) },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
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
