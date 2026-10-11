package com.passportphotomaker.print.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.data.PhotoSizes
import com.passportphotomaker.print.ui.components.GlassCard
import com.passportphotomaker.print.ui.components.GlassPrimaryButton
import com.passportphotomaker.print.ui.components.GlassSecondaryButton
import com.passportphotomaker.print.ui.components.SectionHeader

@Composable
fun HomeScreen(
    onTakePhoto: () -> Unit,
    onImagePicked: (Uri) -> Unit,
    onSeeAllSizes: () -> Unit,
    onSelectSize: (PhotoSize) -> Unit,
    onPrint: () -> Unit,
    onMyPhotos: () -> Unit,
    onSettings: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onImagePicked(uri) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 120.dp)
    ) {
        Text(
            text = "Passport Photo Maker & Print",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Quick  ·  Easy  ·  Accurate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // Hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2E8BFF), Color(0xFF1677FF), Color(0xFF0B5CD6))
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Make Professional Passport Photos",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Capture or choose a photo, pick a size, and print multiple copies on a single sheet.",
                    color = Color(0xDDFFFFFF),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(18.dp))
                GlassPrimaryButton(
                    text = "Take Photo",
                    icon = Icons.Filled.PhotoCamera,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onTakePhoto
                )
                Spacer(Modifier.height(10.dp))
                GlassSecondaryButton(
                    text = "Choose from Gallery",
                    icon = Icons.Filled.Collections,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader(
            title = "Popular Sizes",
            actionLabel = "See All >",
            onAction = onSeeAllSizes
        )
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(PhotoSizes.all.take(3)) { size ->
                SizeChip(size = size, onClick = { onSelectSize(size) })
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Tools")
        Spacer(Modifier.height(12.dp))

        ActionTile(
            icon = Icons.Filled.Print,
            title = "Print Photos",
            subtitle = "Multiple copies on 4 x 6\" or A4",
            onClick = onPrint
        )
        Spacer(Modifier.height(12.dp))
        ActionTile(
            icon = Icons.Filled.Collections,
            title = "My Photos",
            subtitle = "Saved and printed photos",
            onClick = onMyPhotos
        )
        Spacer(Modifier.height(12.dp))
        ActionTile(
            icon = Icons.Filled.Settings,
            title = "Settings",
            subtitle = "Theme, default paper, units and more",
            onClick = onSettings
        )

        Spacer(Modifier.height(20.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    "All photos are processed on your device and never uploaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SizeChip(size: PhotoSize, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.size(width = 150.dp, height = 120.dp),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                size.name.take(1),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            size.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            size.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E8BFF), Color(0xFF1677FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
