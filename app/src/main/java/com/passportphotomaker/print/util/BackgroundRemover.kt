package com.passportphotomaker.print.util

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A lightweight, fully on-device background remover.
 *
 * It works well for photos taken against a plain, evenly lit wall (the usual
 * case for passport photos): it samples the border pixels to estimate the
 * background colour, then makes every pixel close to that colour transparent.
 *
 * LIMITATION: this is a colour-distance (chroma-key style) approach, not a
 * machine-learning subject segmentation. It will not cleanly cut out a subject
 * from a busy or multi-coloured background. For such photos the app offers a
 * solid background colour instead, and the README documents this honestly.
 */
object BackgroundRemover {

    data class Result(val bitmap: Bitmap, val sampledColor: Int, val removedFraction: Float)

    fun remove(source: Bitmap, tolerance: Float = 0.16f, feather: Float = 0.05f): Result {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        // Estimate background colour from the border pixels.
        var rSum = 0L; var gSum = 0L; var bSum = 0L; var count = 0L
        fun sample(x: Int, y: Int) {
            val c = pixels[y * w + x]
            rSum += (c shr 16) and 0xFF
            gSum += (c shr 8) and 0xFF
            bSum += c and 0xFF
            count++
        }
        val step = (minOf(w, h) / 40).coerceAtLeast(1)
        for (x in 0 until w step step) { sample(x, 0); sample(x, h - 1) }
        for (y in 0 until h step step) { sample(0, y); sample(w - 1, y) }
        if (count == 0L) return Result(source, 0, 0f)

        val bgR = (rSum / count).toInt()
        val bgG = (gSum / count).toInt()
        val bgB = (bSum / count).toInt()
        val sampled = (0xFF shl 24) or (bgR shl 16) or (bgG shl 8) or bgB

        val maxDist = sqrt(3f) * 255f
        var removed = 0
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val dr = (r - bgR).toFloat()
            val dg = (g - bgG).toFloat()
            val db = (b - bgB).toFloat()
            val dist = sqrt(dr * dr + dg * dg + db * db) / maxDist
            when {
                dist <= tolerance -> {
                    pixels[i] = c and 0x00FFFFFF // fully transparent
                    removed++
                }
                dist <= tolerance + feather -> {
                    // Soft edge: scale existing alpha down.
                    val t = (dist - tolerance) / feather
                    val alpha = (((c ushr 24) and 0xFF) * t).toInt().coerceIn(0, 255)
                    pixels[i] = (alpha shl 24) or (c and 0x00FFFFFF)
                    removed++
                }
            }
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return Result(out, sampled, removed.toFloat() / (w * h))
    }

    /** Returns true if the two colours are visually close (used by the UI). */
    fun isSimilar(a: Int, b: Int, tolerance: Int = 24): Boolean {
        return abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)) <= tolerance &&
            abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)) <= tolerance &&
            abs((a and 0xFF) - (b and 0xFF)) <= tolerance
    }
}
