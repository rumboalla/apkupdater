package com.apkupdater.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apkupdater.R
import com.apkupdater.viewmodel.UpdatesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(viewModel: UpdatesViewModel = koinViewModel()) {
	TopAppBar(title = { Text(stringResource(R.string.tab_updates)) })
}