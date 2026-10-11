package com.passportphotomaker.print.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Size
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

    const val MM_PER_INCH = 25.4f

    fun mmToPx(mm: Float, dpi: Int): Int = (mm / MM_PER_INCH * dpi).toInt().coerceAtLeast(1)

    fun pxToMm(px: Int, dpi: Int): Float = px.toFloat() / dpi * MM_PER_INCH

    /** Loads a bitmap from a content Uri, downsampled so the largest side <= maxDim. */
    fun loadBitmap(context: Context, uri: Uri, maxDim: Int = 2400): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(
                    source,
                    android.graphics.ImageDecoder.OnHeaderDecodedListener { decoder, info, _ ->
                        val (w, h) = info.size.width to info.size.height
                        val scale = sampleScale(max(w, h), maxDim)
                        decoder.setTargetSampleSize(scale)
                    }
                ).copy(Bitmap.Config.ARGB_8888, false)
            } else {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
                val scale = sampleScale(max(opts.outWidth, opts.outHeight), maxDim)
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = scale
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bmp = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOpts)
                } ?: return null
                applyExifOrientation(context, uri, bmp)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromFile(file: java.io.File, maxDim: Int = 2400): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, opts)
            val scale = sampleScale(max(opts.outWidth, opts.outHeight), maxDim)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = scale
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
        } catch (e: Exception) {
            null
        }
    }

    private fun sampleScale(largest: Int, target: Int): Int {
        if (largest <= target) return 1
        var scale = 1
        while (largest / (scale * 2) >= target) scale *= 2
        return scale
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bmp
            }
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } catch (e: Exception) {
            bmp
        }
    }

    fun rotate(source: Bitmap, degrees: Float): Bitmap {
        if (degrees % 360f == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun flip(source: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) postScale(-1f, 1f) else postScale(1f, -1f)
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Builds a 4x5 colour matrix (row-major, same layout as android.graphics.ColorMatrix
     * and androidx.compose.ui.graphics.ColorMatrix) combining saturation, then contrast,
     * then brightness. Used both for the live preview filter and the final render so the
     * preview always matches the output.
     *
     * @param brightness in [-1, 1]
     * @param contrast in [0.5, 2] (1 = unchanged)
     * @param saturation in [0, 2] (1 = unchanged)
     */
    fun colorMatrixArray(brightness: Float, contrast: Float, saturation: Float): FloatArray {
        val s = saturation
        val sr = (1f - s) * 0.213f
        val sg = (1f - s) * 0.715f
        val sb = (1f - s) * 0.072f
        val sat = floatArrayOf(
            sr + s, sg, sb, 0f, 0f,
            sr, sg + s, sb, 0f, 0f,
            sr, sg, sb + s, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        val c = contrast
        val t = (1f - c) * 128f + brightness * 255f
        val con = floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f
        )
        return multiplyColorMatrix(con, sat)
    }

    private fun multiplyColorMatrix(a: FloatArray, b: FloatArray): FloatArray {
        val r = FloatArray(20)
        for (row in 0 until 4) {
            for (col in 0 until 5) {
                var sum = 0f
                for (k in 0 until 4) sum += a[row * 5 + k] * b[k * 5 + col]
                if (col == 4) sum += a[row * 5 + 4]
                r[row * 5 + col] = sum
            }
        }
        return r
    }

    fun adjustWithMatrix(source: Bitmap, matrixArray: FloatArray): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(matrixArray))
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    fun adjust(source: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap =
        adjustWithMatrix(source, colorMatrixArray(brightness, contrast, saturation))

    /** Center-crops to the given aspect ratio (w/h). */
    fun centerCropToAspect(source: Bitmap, aspect: Float): Bitmap {
        val w = source.width
        val h = source.height
        val current = w.toFloat() / h
        val out: Bitmap = if (current > aspect) {
            val targetW = (h * aspect).toInt().coerceAtLeast(1)
            val x = (w - targetW) / 2
            Bitmap.createBitmap(source, x, 0, targetW, h)
        } else {
            val targetH = (w / aspect).toInt().coerceAtLeast(1)
            val y = (h - targetH) / 2
            Bitmap.createBitmap(source, 0, y, w, targetH)
        }
        return out
    }

    /** Crops a sub-rectangle (in source pixel coordinates) from the bitmap. */
    fun cropRegion(source: Bitmap, rect: Rect): Bitmap {
        val left = rect.left.coerceIn(0, source.width - 1)
        val top = rect.top.coerceIn(0, source.height - 1)
        val right = rect.right.coerceIn(left + 1, source.width)
        val bottom = rect.bottom.coerceIn(top + 1, source.height)
        return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
    }

    fun scale(source: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(source, width.coerceAtLeast(1), height.coerceAtLeast(1), true)

    /**
     * Scales and crops so the image exactly fills a target box without distortion
     * (centre-weighted "cover" fit). Faces keep their proportions.
     */
    fun fitToBox(source: Bitmap, boxW: Int, boxH: Int): Bitmap {
        val scale = max(boxW.toFloat() / source.width, boxH.toFloat() / source.height)
        val scaledW = (source.width * scale).toInt().coerceAtLeast(boxW)
        val scaledH = (source.height * scale).toInt().coerceAtLeast(boxH)
        val scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)
        val x = (scaledW - boxW) / 2
        val y = (scaledH - boxH) / 2
        return Bitmap.createBitmap(scaled, x, y, boxW, boxH)
    }

    /** Composites [foreground] (which may have transparency) onto a solid [background] colour. */
    fun flatten(foreground: Bitmap, backgroundColor: Int): Bitmap {
        val out = Bitmap.createBitmap(foreground.width, foreground.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(foreground, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        return out
    }

    /**
     * Produces a passport-style sheet image for preview purposes. The same layout
     * maths is shared with the PDF exporter.
     */
    fun composePreviewSheet(
        photo: Bitmap,
        photoWmm: Float,
        photoHmm: Float,
        paper: com.passportphotomaker.print.data.PaperSize,
        dpi: Int
    ): Bitmap {
        val layout = PrintLayout.compute(photoWmm, photoHmm, paper, dpi)
        val sheetW = mmToPx(paper.widthMm, dpi)
        val sheetH = mmToPx(paper.heightMm, dpi)
        val sheet = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        for (row in 0 until layout.rows) {
            for (col in 0 until layout.cols) {
                val x = layout.marginX + col * (layout.cellW + layout.gapX)
                val y = layout.marginY + row * (layout.cellH + layout.gapY)
                val cellRect = RectF(
                    x.toFloat(),
                    y.toFloat(),
                    (x + layout.cellW).toFloat(),
                    (y + layout.cellH).toFloat()
                )
                canvas.drawBitmap(photo, null, cellRect, paint)
                canvas.drawRect(cellRect, Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.argb(60, 120, 130, 150)
                })
            }
        }
        return sheet
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 90): String {
        val out = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun describeSize(bitmap: Bitmap): Size = Size(bitmap.width, bitmap.height)

    /** Applies a source sub-rect onto a canvas filling the destination rect (cover fit). */
    fun drawCover(canvas: Canvas, bitmap: Bitmap, dst: RectF) {
        val srcAspect = bitmap.width.toFloat() / bitmap.height
        val dstAspect = dst.width() / dst.height()
        val src: Rect
        if (srcAspect > dstAspect) {
            val cropW = (bitmap.height * dstAspect).toInt()
            val x = (bitmap.width - cropW) / 2
            src = Rect(x, 0, x + cropW, bitmap.height)
        } else {
            val cropH = (bitmap.width / dstAspect).toInt()
            val y = (bitmap.height - cropH) / 2
            src = Rect(0, y, bitmap.width, y + cropH)
        }
        canvas.drawBitmap(bitmap, src, dst, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    fun openStream(context: Context, uri: Uri): InputStream? =
        runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
}
