package com.passportphotomaker.print.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.passportphotomaker.print.data.PhotoItem
import com.passportphotomaker.print.data.PhotoSizes
import java.io.File

@Composable
fun MyPhotosScreen(
    photos: List<PhotoItem>,
    fileFor: (PhotoItem) -> File,
    uriFor: (File) -> android.net.Uri,
    onDelete: (PhotoItem) -> Unit
) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("All") }
    var actionTarget by remember { mutableStateOf<PhotoItem?>(null) }

    val filtered = remember(photos, category) {
        when (category) {
            "All" -> photos
            "Passport" -> photos.filter { it.category == PhotoSizes.CAT_PASSPORT }
            "Visa" -> photos.filter { it.category == PhotoSizes.CAT_VISA }
            else -> photos.filter {
                it.category != PhotoSizes.CAT_PASSPORT && it.category != PhotoSizes.CAT_VISA
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "My Photos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Passport", "Visa", "Other").forEach { cat ->
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
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No photos yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Photos you save will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(item.widthMm / item.heightMm)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .clickable { actionTarget = item }
                    ) {
                        AsyncImage(
                            model = fileFor(item),
                            contentDescription = item.sizeLabel,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    actionTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { actionTarget = null },
            title = { Text(item.sizeLabel) },
            text = { Text("Share or delete this photo.") },
            confirmButton = {
                TextButton(onClick = {
                    val file = fileFor(item)
                    val uri = uriFor(file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share photo"))
                    actionTarget = null
                }) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDelete(item)
                    actionTarget = null
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Delete")
                }
            }
        )
    }
}
