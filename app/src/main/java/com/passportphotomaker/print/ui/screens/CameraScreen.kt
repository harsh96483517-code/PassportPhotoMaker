package com.passportphotomaker.print.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.passportphotomaker.print.ui.components.GlassPrimaryButton
import java.io.File

@Composable
fun CameraScreen(
    showFaceGuide: Boolean,
    onCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onCaptured(uri) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    var boundLens by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                update = { previewView ->
                    if (boundLens != lensFacing) {
                        boundLens = lensFacing
                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(previewView.context)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val selector = CameraSelector.Builder()
                                    .requireLensFacing(lensFacing)
                                    .build()
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageCapture
                                )
                            } catch (_: Exception) {
                                // Binding can fail on devices without the requested camera.
                            }
                        }, ContextCompat.getMainExecutor(previewView.context))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (showFaceGuide) {
                FaceGuideOverlay(Modifier.fillMaxSize())
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(Icons.Filled.ArrowBack, onBack)
                Spacer(Modifier.size(12.dp))
                Text(
                    "Keep your face in the frame",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x66000000))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(Icons.Filled.Collections) {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }

                // Shutter
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { capturePhoto(context, imageCapture, onCaptured) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                CircleIconButton(Icons.Filled.Cameraswitch) {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                }
            }
        } else {
            PermissionRequest(onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) })
        }
    }
}

private fun capturePhoto(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onCaptured: (Uri) -> Unit
) {
    val photoFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                onCaptured(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                // Silently ignore; the user can retry the capture.
            }
        }
    )
}

@Composable
private fun FaceGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val ovalW = w * 0.66f
        val ovalH = h * 0.52f
        val left = (w - ovalW) / 2f
        val top = h * 0.18f
        drawOval(
            color = Color(0x99FFFFFF),
            topLeft = Offset(left, top),
            size = Size(ovalW, ovalH),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
            )
        )
        // Centre line guide
        drawLine(
            color = Color(0x55FFFFFF),
            start = Offset(w / 2f, top),
            end = Offset(w / 2f, top + ovalH),
            strokeWidth = 1.5f
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun PermissionRequest(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Camera permission needed",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We need access to your camera to capture a passport photo. Photos stay on your device.",
            color = Color(0xCCFFFFFF),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        GlassPrimaryButton(text = "Grant permission", onClick = onGrant)
    }
}
