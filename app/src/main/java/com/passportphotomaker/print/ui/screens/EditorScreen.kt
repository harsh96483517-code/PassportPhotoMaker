package com.passportphotomaker.print.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.ui.components.GlassPrimaryButton
import com.passportphotomaker.print.util.BackgroundRemover
import com.passportphotomaker.print.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private enum class EditorMode { CROP, ADJUST, BACKGROUND }

@Composable
fun EditorScreen(
    sourceUri: android.net.Uri?,
    size: PhotoSize,
    onBack: () -> Unit,
    onChangeSize: () -> Unit,
    onDone: (Bitmap) -> Unit
) {
    val context = LocalContext.current

    var full by remember { mutableStateOf<Bitmap?>(null) }
    var originalFull by remember { mutableStateOf<Bitmap?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    // Editor controls
    var mode by remember { mutableStateOf(EditorMode.CROP) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var removeBg by remember { mutableStateOf(false) }
    var bgColor by remember { mutableStateOf(AndroidColor.WHITE) }

    // Crop transform (in preview space)
    var userScale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var frameSize by remember { mutableStateOf(IntSize.Zero) }
    var processing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun buildPreview(src: Bitmap, removeBackground: Boolean, bg: Int): Bitmap =
        withContext(Dispatchers.Default) {
            val downscaled = if (max(src.width, src.height) > 1080) {
                val s = 1080f / max(src.width, src.height)
                ImageUtils.scale(src, (src.width * s).toInt(), (src.height * s).toInt())
            } else src
            if (removeBackground) {
                val removed = BackgroundRemover.remove(downscaled).bitmap
                ImageUtils.flatten(removed, bg)
            } else downscaled
        }

    // Load the source image once.
    LaunchedEffect(sourceUri) {
        loading = true
        error = false
        val loaded = sourceUri?.let { ImageUtils.loadBitmap(context, it) }
        if (loaded == null) {
            error = true
            loading = false
            return@LaunchedEffect
        }
        full = loaded
        originalFull = loaded
        preview = buildPreview(loaded, removeBg, bgColor)
        userScale = 1f
        offset = Offset.Zero
        loading = false
    }

    // Rebuild the preview whenever background settings change.
    LaunchedEffect(removeBg, bgColor, full) {
        full?.let { preview = buildPreview(it, removeBg, bgColor) }
    }

    val colorMatrix = remember(brightness, contrast, saturation) {
        ColorMatrix(ImageUtils.colorMatrixArray(brightness, contrast, saturation))
    }
    val colorFilter = remember(colorMatrix) { ColorFilter.colorMatrix(colorMatrix) }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(Icons.Filled.ArrowBack, onBack)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Crop & Adjust",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "${size.name} · ${size.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onChangeSize() }
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        val f = full
                        if (f != null && !processing) {
                            processing = true
                            val pv = preview
                            val fs = frameSize
                            val us = userScale
                            val off = offset
                            val b = brightness
                            val c = contrast
                            val s = saturation
                            val rb = removeBg
                            val bg = bgColor
                            scope.launch {
                                val result = withContext(Dispatchers.Default) {
                                    val cropped = applyCrop(f, pv, fs, us, off)
                                    val adjusted = ImageUtils.adjustWithMatrix(
                                        cropped,
                                        ImageUtils.colorMatrixArray(b, c, s)
                                    )
                                    if (rb) {
                                        val removed = BackgroundRemover.remove(adjusted).bitmap
                                        ImageUtils.flatten(removed, bg)
                                    } else adjusted
                                }
                                processing = false
                                onDone(result)
                            }
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Preview / crop canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> Text(
                    "Loading photo…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                error || preview == null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No photo selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Go back and take a photo or choose one from your gallery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    val pv = preview!!
                    val imgBitmap = remember(pv) { pv.asImageBitmap() }
                    val aspect = size.aspectRatio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .onSizeChanged { frameSize = it }
                            .pointerInput(pv) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(1f, 6f)
                                    offset = clampOffset(
                                        imgW = pv.width,
                                        imgH = pv.height,
                                        frame = frameSize,
                                        scale = userScale,
                                        candidate = offset + pan
                                    )
                                }
                            }
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val cover = max(this.size.width / pv.width, this.size.height / pv.height)
                            val eff = cover * userScale
                            val dispW = pv.width * eff
                            val dispH = pv.height * eff
                            val left = this.size.width / 2f + offset.x - dispW / 2f
                            val top = this.size.height / 2f + offset.y - dispH / 2f
                            drawImage(
                                image = imgBitmap,
                                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                                dstSize = IntSize(dispW.roundToInt(), dispH.roundToInt()),
                                colorFilter = colorFilter
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pinch to zoom, drag to reposition",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeChip("Crop", Icons.Filled.Crop, mode == EditorMode.CROP, Modifier.weight(1f)) { mode = EditorMode.CROP }
            ModeChip("Adjust", Icons.Filled.Tune, mode == EditorMode.ADJUST, Modifier.weight(1f)) { mode = EditorMode.ADJUST }
            ModeChip("Background", Icons.Filled.Wallpaper, mode == EditorMode.BACKGROUND, Modifier.weight(1f)) { mode = EditorMode.BACKGROUND }
        }

        // Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (mode) {
                EditorMode.CROP -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToolButton("Rotate", Icons.Filled.RotateRight, Modifier.weight(1f)) {
                        full = full?.let { ImageUtils.rotate(it, 90f) }
                        userScale = 1f; offset = Offset.Zero
                    }
                    ToolButton("Flip", Icons.Filled.Flip, Modifier.weight(1f)) {
                        full = full?.let { ImageUtils.flip(it, true) }
                        userScale = 1f; offset = Offset.Zero
                    }
                    ToolButton("Reset", Icons.Filled.Refresh, Modifier.weight(1f)) {
                        full = originalFull
                        userScale = 1f; offset = Offset.Zero
                    }
                }

                EditorMode.ADJUST -> Column {
                    SliderRow("Brightness", brightness, -0.5f, 0.5f) { brightness = it }
                    SliderRow("Contrast", contrast, 0.5f, 1.8f) { contrast = it }
                    SliderRow("Saturation", saturation, 0f, 2f) { saturation = it }
                    Spacer(Modifier.height(6.dp))
                    ToolButton("Reset adjustments", Icons.Filled.Refresh, Modifier.fillMaxWidth()) {
                        brightness = 0f; contrast = 1f; saturation = 1f
                    }
                }

                EditorMode.BACKGROUND -> Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Remove background",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Best on plain, evenly lit backgrounds",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = removeBg, onCheckedChange = { removeBg = it })
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Background colour",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        bgSwatches.forEach { (name, argb) ->
                            ColorSwatch(name, argb, bgColor == argb) { bgColor = argb }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Note: automatic cut-out uses a colour-distance method and may not be perfect on busy backgrounds.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private val bgSwatches = listOf(
    "White" to AndroidColor.WHITE,
    "Sky" to AndroidColor.parseColor("#DCEBFF"),
    "Light grey" to AndroidColor.parseColor("#F0F0F0"),
    "Blue" to AndroidColor.parseColor("#1677FF")
)

private fun clampOffset(imgW: Int, imgH: Int, frame: IntSize, scale: Float, candidate: Offset): Offset {
    if (frame.width == 0 || frame.height == 0) return candidate
    val coverScale = max(frame.width.toFloat() / imgW, frame.height.toFloat() / imgH)
    val eff = coverScale * scale
    val dispW = imgW * eff
    val dispH = imgH * eff
    val maxX = ((dispW - frame.width) / 2f).coerceAtLeast(0f)
    val maxY = ((dispH - frame.height) / 2f).coerceAtLeast(0f)
    return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
}

/**
 * Maps the visible crop frame back to source pixels and crops the full-resolution
 * bitmap. [preview] is the (possibly downscaled) bitmap shown on screen; the result
 * is scaled up to [full]'s resolution so no quality is lost.
 */
private fun applyCrop(
    full: Bitmap,
    preview: Bitmap?,
    frame: IntSize,
    userScale: Float,
    offset: Offset
): Bitmap {
    val pv = preview ?: return full
    if (frame.width == 0 || frame.height == 0) return full
    val coverScale = max(frame.width.toFloat() / pv.width, frame.height.toFloat() / pv.height)
    val eff = coverScale * userScale
    val left = frame.width / 2f + offset.x - (pv.width * eff) / 2f
    val top = frame.height / 2f + offset.y - (pv.height * eff) / 2f

    val nLeft = ((0f - left) / eff) / pv.width
    val nTop = ((0f - top) / eff) / pv.height
    val nRight = ((frame.width - left) / eff) / pv.width
    val nBottom = ((frame.height - top) / eff) / pv.height

    val rect = android.graphics.Rect(
        (nLeft * full.width).roundToInt().coerceIn(0, full.width - 1),
        (nTop * full.height).roundToInt().coerceIn(0, full.height - 1),
        (nRight * full.width).roundToInt().coerceIn(1, full.width),
        (nBottom * full.height).roundToInt().coerceIn(1, full.height)
    )
    return ImageUtils.cropRegion(full, rect)
}

@Composable
private fun IconCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                String.format("%.2f", value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(value = value, onValueChange = onChange, valueRange = min..max)
    }
}

@Composable
private fun ColorSwatch(name: String, argb: Int, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(argb))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable { onClick() }
        )
        Spacer(Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
