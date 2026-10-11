package com.passportphotomaker.print.data

/** Metadata for a saved photo. The image bytes live in a sibling JPG file. */
data class PhotoItem(
    val id: String,
    val fileName: String,
    val sizeLabel: String,
    val widthMm: Float,
    val heightMm: Float,
    val category: String,
    val createdAt: Long
)

/** Print sheet presets. Dimensions are in millimetres at 300 DPI output. */
enum class PaperSize(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float
) {
    R4("4R (4 x 6 inch)", 101.6f, 152.4f),
    A4("A4 (210 x 297 mm)", 210f, 297f),
    LETTER("Letter (8.5 x 11 inch)", 215.9f, 279.4f)
}

enum class PrintQuality(val displayName: String, val dpi: Int) {
    STANDARD("Standard", 200),
    HIGH("High", 300),
    ULTRA("Ultra", 400)
}
