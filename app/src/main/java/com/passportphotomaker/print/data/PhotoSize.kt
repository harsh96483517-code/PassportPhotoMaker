package com.passportphotomaker.print.data

/**
 * A configurable ID-photo size. Dimensions are stored in millimetres; the app
 * converts them to pixels at the requested DPI when rendering.
 *
 * NOTE: The sizes shipped here follow widely published conventions, but document
 * authorities may change their requirements. The app does not claim official
 * compliance — always confirm against the issuing authority's current guidance.
 */
data class PhotoSize(
    val id: String,
    val name: String,
    val widthMm: Float,
    val heightMm: Float,
    val category: String,
    val note: String
) {
    val aspectRatio: Float get() = widthMm / heightMm

    val label: String
        get() {
            val w = if (widthMm % 1f == 0f) widthMm.toInt().toString() else widthMm.toString()
            val h = if (heightMm % 1f == 0f) heightMm.toInt().toString() else heightMm.toString()
            return "$w x $h mm"
        }
}

object PhotoSizes {
    const val CAT_PASSPORT = "Passport"
    const val CAT_VISA = "Visa"
    const val CAT_ID = "ID Card"
    const val CAT_CUSTOM = "Custom"

    val all: List<PhotoSize> = listOf(
        PhotoSize("passport_51", "Passport", 51f, 51f, CAT_PASSPORT, "For Indian passport and many countries"),
        PhotoSize("visa_35_45", "Visa", 35f, 45f, CAT_VISA, "For Schengen, USA, UK, Canada"),
        PhotoSize("dl_30_40", "Driving License", 30f, 40f, CAT_ID, "For Indian driving licence"),
        PhotoSize("pan_20_20", "PAN Card", 20f, 20f, CAT_ID, "For Indian PAN card"),
        PhotoSize("aadhaar_35_45", "Aadhaar Card", 35f, 45f, CAT_ID, "For Indian Aadhaar"),
        PhotoSize("student_25_30", "Student ID", 25f, 30f, CAT_ID, "For school or college ID")
    )

    val categories: List<String> = listOf("All", CAT_PASSPORT, CAT_VISA, CAT_ID, CAT_CUSTOM)

    val default: PhotoSize = all.first()

    fun byId(id: String): PhotoSize = all.firstOrNull { it.id == id } ?: default

    fun custom(widthMm: Float, heightMm: Float): PhotoSize = PhotoSize(
        id = "custom_${widthMm}_$heightMm",
        name = "Custom",
        widthMm = widthMm,
        heightMm = heightMm,
        category = CAT_CUSTOM,
        note = "Custom size"
    )
}
