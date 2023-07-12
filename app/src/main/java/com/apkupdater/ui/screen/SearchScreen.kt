package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen() {
	SearchTopBar()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchTopBar() = Box {
	val keyboardController = LocalSoftwareKeyboardController.current
	var value by remember { mutableStateOf("") }
	OutlinedTextField(
		value = value,
		onValueChange = {
			value = it
		},
		modifier = Modifier.fillMaxWidth().padding(8.dp),
		label = { Text("Search") },
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
		keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
	)
}
