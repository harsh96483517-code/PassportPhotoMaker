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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.print.PrintHelper
import com.passportphotomaker.print.data.PaperSize
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.data.PrintQuality
import com.passportphotomaker.print.ui.components.GlassCard
import com.passportphotomaker.print.ui.components.GlassPrimaryButton
import com.passportphotomaker.print.ui.components.GlassSecondaryButton
import com.passportphotomaker.print.util.ImageUtils
import com.passportphotomaker.print.util.PdfExporter
import java.io.File

@Composable
fun PrintScreen(
    editedFile: File?,
    size: PhotoSize,
    initialPaper: PaperSize,
    initialQuality: PrintQuality,
    onBack: () -> Unit,
    onNeedPhoto: () -> Unit,
    onExportPdf: (Bitmap, PhotoSize, PaperSize, PrintQuality, Int) -> Unit
) {
    val context = LocalContext.current
    var paper by remember { mutableStateOf(initialPaper) }
    var quality by remember { mutableStateOf(initialQuality) }
    var pages by remember { mutableIntStateOf(1) }
    var grayscale by remember { mutableStateOf(false) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var sheetPreview by remember { mutableStateOf<Bitmap?>(null) }
    var copiesPerSheet by remember { mutableIntStateOf(0) }

    LaunchedEffect(editedFile, size) {
        photo = editedFile?.let { ImageUtils.loadBitmapFromFile(it, 2000) }
    }

    LaunchedEffect(photo, paper, quality, grayscale) {
        val p = photo
        if (p == null) {
            sheetPreview = null
            copiesPerSheet = 0
            return@LaunchedEffect
        }
        val base = ImageUtils.fitToBox(
            p,
            ImageUtils.mmToPx(size.widthMm, quality.dpi),
            ImageUtils.mmToPx(size.heightMm, quality.dpi)
        )
        val finalPhoto = if (grayscale) {
            ImageUtils.adjust(base, 0f, 1f, 0f)
        } else base
        copiesPerSheet = PdfExporter.copiesPerSheet(size.widthMm, size.heightMm, paper, quality.dpi)
        // Preview sheet at a modest DPI for performance.
        sheetPreview = ImageUtils.composePreviewSheet(finalPhoto, size.widthMm, size.heightMm, paper, 100)
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
                "Print Photos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (photo == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No photo to print yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Create a passport photo first, then come back to print it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    GlassPrimaryButton(text = "Create a photo", onClick = onNeedPhoto)
                }
            }
        } else {

        // Sheet preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val sheet = sheetPreview
            if (sheet != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(paper.widthMm / paper.heightMm)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        bitmap = sheet.asImageBitmap(),
                        contentDescription = "Print sheet preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "About $copiesPerSheet photos per sheet",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Print Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            Text("Paper size", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaperSize.values().forEach { p ->
                    OptionChip(p.displayName.substringBefore(" ("), p == paper, Modifier.weight(1f)) { paper = p }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Print quality", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrintQuality.values().forEach { q ->
                    OptionChip(q.displayName, q == quality, Modifier.weight(1f)) { quality = q }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Copies (pages)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3, 5).forEach { n ->
                    OptionChip("$n", pages == n, Modifier.weight(1f)) { pages = n }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Colour mode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (grayscale) "Grayscale" else "Colour",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                OptionChip(if (grayscale) "Switch to colour" else "Switch to grayscale", false, Modifier) {
                    grayscale = !grayscale
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Column(Modifier.padding(horizontal = 16.dp)) {
            GlassPrimaryButton(
                text = "Print Now",
                icon = Icons.Filled.Print,
                modifier = Modifier.fillMaxWidth()
            ) {
                val sheet = sheetPreview
                if (sheet != null) {
                    val helper = PrintHelper(context).apply {
                        scaleMode = PrintHelper.SCALE_MODE_FIT
                        colorMode = if (grayscale) PrintHelper.COLOR_MODE_MONOCHROME else PrintHelper.COLOR_MODE_COLOR
                    }
                    helper.printBitmap("Passport photos - ${size.name}", sheet)
                }
            }
            Spacer(Modifier.height(10.dp))
            GlassSecondaryButton(
                text = "Save as PDF",
                icon = Icons.Filled.PictureAsPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                val base = photo ?: return@GlassSecondaryButton
                val finalPhoto = ImageUtils.fitToBox(
                    base,
                    ImageUtils.mmToPx(size.widthMm, quality.dpi),
                    ImageUtils.mmToPx(size.heightMm, quality.dpi)
                )
                val photoToExport = if (grayscale) ImageUtils.adjust(finalPhoto, 0f, 1f, 0f) else finalPhoto
                onExportPdf(photoToExport, size, paper, quality, pages)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "You can also save as PDF and print later at any photo studio or home printer.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun OptionChip(
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
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
