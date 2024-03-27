package com.apkupdater.data.snack

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals


class TextSnack(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = true
): SnackbarVisuals
