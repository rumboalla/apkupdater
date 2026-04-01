package com.durcheins.apkupdater.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.durcheins.apkupdater.data.ui.AppUpdate
import java.util.Locale

/**
 * Zeigt den Download-Fortschritt in der App-Kachel an.
 * Bei bekannter Gesamtgröße: LinearProgressIndicator mit Prozent, MB/MB und MB/s
 * Bei unbekannter Größe: CircularProgressIndicator (Spinner)
 */
@Composable
fun DownloadProgressOverlay(app: AppUpdate) {
    if (!app.isInstalling) return

    val hasTotal = app.total > 0L
    val hasProgress = app.progress > 0L

    if (hasTotal && hasProgress) {
        val fraction = (app.progress.toFloat() / app.total.toFloat()).coerceIn(0f, 1f)
        val animatedFraction by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 300),
            label = "downloadProgress"
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${formatBytes(app.progress)} / ${formatBytes(app.total)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedFraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                if (app.speed > 0L) {
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatBytes(app.speed)}/s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val remaining = app.total - app.progress
                        if (app.speed > 0L && remaining > 0L) {
                            val etaSeconds = remaining / app.speed
                            Text(
                                text = formatEta(etaSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Unbekannte Größe: nur Spinner
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> String.format(Locale.getDefault(), "%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024L         -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1_024.0)
        else                    -> "$bytes B"
    }
}

private fun formatEta(seconds: Long): String {
    return when {
        seconds >= 3600 -> String.format(Locale.getDefault(), "%dh %02dm", seconds / 3600, (seconds % 3600) / 60)
        seconds >= 60   -> String.format(Locale.getDefault(), "%dm %02ds", seconds / 60, seconds % 60)
        else            -> "${seconds}s"
    }
}
