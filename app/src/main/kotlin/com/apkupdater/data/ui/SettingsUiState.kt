package com.apkupdater.data.ui


sealed class SettingsUiState {
    object Settings : SettingsUiState()
    object About : SettingsUiState()
}
