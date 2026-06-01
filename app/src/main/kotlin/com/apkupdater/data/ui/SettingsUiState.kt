package com.apkupdater.data.ui


sealed class SettingsUiState {
    data object Settings : SettingsUiState()
    data object About : SettingsUiState()
}
