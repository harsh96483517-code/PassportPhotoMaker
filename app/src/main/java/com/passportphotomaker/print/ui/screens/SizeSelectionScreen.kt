package com.passportphotomaker.print.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.data.PhotoSizes
import com.passportphotomaker.print.ui.components.GlassCard
import com.passportphotomaker.print.ui.components.GlassPrimaryButton

@Composable
fun SizeSelectionScreen(
    selectedId: String,
    onSelect: (PhotoSize) -> Unit,
    onBack: () -> Unit
) {
    var category by remember { mutableStateOf("All") }
    var showCustom by remember { mutableStateOf(false) }

    val filtered = remember(category) {
        if (category == "All") PhotoSizes.all else PhotoSizes.all.filter { it.category == category }
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.size(12.dp))
            Text(
                "ID Photo Sizes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoSizes.categories.forEach { cat ->
                val selected = cat == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { category = cat }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        cat,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { size ->
                SizeRow(
                    size = size,
                    selected = size.id == selectedId,
                    onClick = { onSelect(size) }
                )
            }
            item {
                GlassPrimaryButton(
                    text = "Custom Size",
                    icon = Icons.Filled.Straighten,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showCustom = true }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (showCustom) {
        CustomSizeDialog(
            onDismiss = { showCustom = false },
            onConfirm = { w, h ->
                showCustom = false
                onSelect(PhotoSizes.custom(w, h))
            }
        )
    }
}

@Composable
private fun SizeRow(size: PhotoSize, selected: Boolean, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    size.name.take(1),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${size.name} (${size.label})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    size.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CustomSizeDialog(onDismiss: () -> Unit, onConfirm: (Float, Float) -> Unit) {
    var width by remember { mutableStateOf("35") }
    var height by remember { mutableStateOf("45") }
    val w = width.toFloatOrNull()
    val h = height.toFloatOrNull()
    val valid = w != null && h != null && w in 10f..200f && h in 10f..200f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom size") },
        text = {
            Column {
                Text(
                    "Enter the size in millimetres.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Width (mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Height (mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { if (valid) onConfirm(w!!, h!!) }
            ) { Text("Use size") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
