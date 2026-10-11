package com.passportphotomaker.print.util

import com.passportphotomaker.print.data.PaperSize
import kotlin.math.floor

/**
 * Grid layout of ID photos on a print sheet. All values are in pixels at the
 * given DPI. Shared by the on-screen preview and the PDF exporter so the printed
 * result matches what the user sees.
 */
data class SheetLayout(
    val cols: Int,
    val rows: Int,
    val cellW: Int,
    val cellH: Int,
    val gapX: Int,
    val gapY: Int,
    val marginX: Int,
    val marginY: Int
) {
    val totalCopies: Int get() = cols * rows
}

object PrintLayout {

    /**
     * Computes how many copies of a photo of [photoWmm] x [photoHmm] fit on [paper]
     * at [dpi], including a small gutter between photos and a printable margin.
     */
    fun compute(
        photoWmm: Float,
        photoHmm: Float,
        paper: PaperSize,
        dpi: Int,
        gapMm: Float = 3f,
        marginMm: Float = 6f
    ): SheetLayout {
        val cellW = ImageUtils.mmToPx(photoWmm, dpi)
        val cellH = ImageUtils.mmToPx(photoHmm, dpi)
        val gapX = ImageUtils.mmToPx(gapMm, dpi)
        val gapY = ImageUtils.mmToPx(gapMm, dpi)
        val sheetW = ImageUtils.mmToPx(paper.widthMm, dpi)
        val sheetH = ImageUtils.mmToPx(paper.heightMm, dpi)
        val marginX = ImageUtils.mmToPx(marginMm, dpi)
        val marginY = ImageUtils.mmToPx(marginMm, dpi)

        val usableW = sheetW - 2 * marginX
        val usableH = sheetH - 2 * marginY

        val cols = floor((usableW + gapX).toFloat() / (cellW + gapX)).toInt().coerceAtLeast(1)
        val rows = floor((usableH + gapY).toFloat() / (cellH + gapY)).toInt().coerceAtLeast(1)

        // Centre the grid within the usable area.
        val gridW = cols * cellW + (cols - 1) * gapX
        val gridH = rows * cellH + (rows - 1) * gapY
        val centredMarginX = marginX + (usableW - gridW) / 2
        val centredMarginY = marginY + (usableH - gridH) / 2

        return SheetLayout(
            cols = cols,
            rows = rows,
            cellW = cellW,
            cellH = cellH,
            gapX = gapX,
            gapY = gapY,
            marginX = centredMarginX,
            marginY = centredMarginY
        )
    }
}
