package com.passportphotomaker.print.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passportphotomaker.print.data.AppSettings
import com.passportphotomaker.print.data.PaperSize
import com.passportphotomaker.print.data.PrintQuality
import com.passportphotomaker.print.data.ThemeMode
import com.passportphotomaker.print.ui.components.GlassCard

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onTheme: (ThemeMode) -> Unit,
    onPaper: (PaperSize) -> Unit,
    onQuality: (PrintQuality) -> Unit,
    onShowGuide: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 120.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    Chip(
                        label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = settings.themeMode == mode,
                        modifier = Modifier.weight(1f)
                    ) { onTheme(mode) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("Print defaults", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.height(12.dp))
            Text("Default paper", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaperSize.values().forEach { p ->
                    Chip(p.displayName.substringBefore(" ("), settings.defaultPaper == p, Modifier.weight(1f)) { onPaper(p) }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Default quality", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrintQuality.values().forEach { q ->
                    Chip(q.displayName, settings.defaultQuality == q, Modifier.weight(1f)) { onQuality(q) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Face alignment guide", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Show an oval guide on the camera screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = settings.showFaceGuide, onCheckedChange = onShowGuide)
            }
        }

        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(
                "Passport Photo Maker & Print",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Version 1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Your photos are processed and stored only on this device. Nothing is uploaded to any server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Disclaimer: this app does not claim official compliance with any document authority's requirements. Size presets follow widely published conventions — always confirm current requirements with the issuing authority before submitting a photo.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}
