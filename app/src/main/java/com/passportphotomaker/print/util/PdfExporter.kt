package com.passportphotomaker.print.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.passportphotomaker.print.data.PaperSize
import java.io.ByteArrayOutputStream

/**
 * Builds a print-ready, multi-page PDF. Each page holds as many copies of the ID
 * photo as fit on the chosen paper size, laid out by [PrintLayout] at the target DPI.
 * A faint cut guide is drawn around each photo so it can be trimmed accurately.
 */
object PdfExporter {

    const val POINTS_PER_INCH = 72f

    fun exportSheetPdf(
        photo: Bitmap,
        photoWmm: Float,
        photoHmm: Float,
        paper: PaperSize,
        dpi: Int,
        pages: Int = 1
    ): ByteArray {
        val layout = PrintLayout.compute(photoWmm, photoHmm, paper, dpi)
        val pageWpt = paper.widthMm / ImageUtils.MM_PER_INCH * POINTS_PER_INCH
        val pageHpt = paper.heightMm / ImageUtils.MM_PER_INCH * POINTS_PER_INCH
        val pxToPt = POINTS_PER_INCH / dpi.toFloat()

        val document = PdfDocument()
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val guide = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = Color.argb(70, 130, 140, 160)
        }

        val safePages = pages.coerceIn(1, 20)
        for (pageIndex in 0 until safePages) {
            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWpt.toInt().coerceAtLeast(1),
                pageHpt.toInt().coerceAtLeast(1),
                pageIndex + 1
            ).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            for (row in 0 until layout.rows) {
                for (col in 0 until layout.cols) {
                    val xPx = layout.marginX + col * (layout.cellW + layout.gapX)
                    val yPx = layout.marginY + row * (layout.cellH + layout.gapY)
                    val dst = RectF(
                        xPx * pxToPt,
                        yPx * pxToPt,
                        (xPx + layout.cellW) * pxToPt,
                        (yPx + layout.cellH) * pxToPt
                    )
                    ImageUtils.drawCover(canvas, photo, dst)
                    canvas.drawRect(dst, guide)
                }
            }
            document.finishPage(page)
        }

        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    /** Convenience: total copies that will be printed across all pages. */
    fun copiesPerSheet(photoWmm: Float, photoHmm: Float, paper: PaperSize, dpi: Int): Int =
        PrintLayout.compute(photoWmm, photoHmm, paper, dpi).totalCopies
}
