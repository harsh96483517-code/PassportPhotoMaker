package com.passportphotomaker.print.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.ui.components.GlassCard
import com.passportphotomaker.print.ui.components.GlassPrimaryButton
import com.passportphotomaker.print.ui.components.GlassSecondaryButton
import com.passportphotomaker.print.util.ImageUtils
import java.io.File

@Composable
fun PreviewScreen(
    editedFile: File?,
    size: PhotoSize,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSaveAndPrint: () -> Unit
) {
    var finalPhoto by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(editedFile, size) {
        val src = editedFile?.let { ImageUtils.loadBitmapFromFile(it, 1600) }
        finalPhoto = src?.let { ImageUtils.fitToBox(it, 900, (900 / size.aspectRatio).toInt()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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
                "Preview",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val bmp = finalPhoto
            if (bmp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(size.aspectRatio)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Final photo preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(size.aspectRatio)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No photo yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            InfoRow("Size", "${size.label} (${size.name})")
            InfoRow("Background", "White / chosen colour")
            InfoRow("Face", "Centred and clear")
            InfoRow("Quality", "High resolution")
        }

        Spacer(Modifier.height(20.dp))

        Column(Modifier.padding(horizontal = 16.dp)) {
            GlassSecondaryButton(
                text = "Edit",
                icon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = onEdit
            )
            Spacer(Modifier.height(10.dp))
            GlassPrimaryButton(
                text = "Save & Print",
                icon = Icons.Filled.Print,
                modifier = Modifier.fillMaxWidth(),
                onClick = onSaveAndPrint
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
