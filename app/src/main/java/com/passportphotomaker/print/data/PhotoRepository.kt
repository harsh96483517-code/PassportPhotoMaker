package com.passportphotomaker.print.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Stores created photos entirely on the device. Nothing is uploaded anywhere.
 *
 * Photos live in `filesDir/photos`, exports (JPG/PDF) in `filesDir/exports`.
 * Metadata is kept in a small JSON index next to the photos.
 */
class PhotoRepository(private val context: Context) {

    private val photosDir: File = File(context.filesDir, "photos").apply { mkdirs() }
    private val exportsDir: File = File(context.filesDir, "exports").apply { mkdirs() }
    private val indexFile: File = File(photosDir, "index.json")
    private val lock = Any()

    fun photoFile(item: PhotoItem): File = File(photosDir, item.fileName)

    fun loadPhotos(): List<PhotoItem> = synchronized(lock) {
        if (!indexFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(indexFile.readText())
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val file = File(photosDir, o.getString("fileName"))
                if (!file.exists()) null else PhotoItem(
                    id = o.getString("id"),
                    fileName = o.getString("fileName"),
                    sizeLabel = o.getString("sizeLabel"),
                    widthMm = o.getDouble("widthMm").toFloat(),
                    heightMm = o.getDouble("heightMm").toFloat(),
                    category = o.optString("category", PhotoSizes.CAT_PASSPORT),
                    createdAt = o.getLong("createdAt")
                )
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePhoto(bitmap: Bitmap, size: PhotoSize): PhotoItem = synchronized(lock) {
        val id = UUID.randomUUID().toString()
        val fileName = "photo_$id.jpg"
        val file = File(photosDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        val item = PhotoItem(
            id = id,
            fileName = fileName,
            sizeLabel = "${size.label} (${size.name})",
            widthMm = size.widthMm,
            heightMm = size.heightMm,
            category = size.category,
            createdAt = System.currentTimeMillis()
        )
        val list = loadPhotos().toMutableList()
        list.add(0, item)
        writeIndex(list)
        item
    }

    fun deletePhoto(item: PhotoItem): Unit = synchronized(lock) {
        photoFile(item).delete()
        val list = loadPhotos().filterNot { it.id == item.id }
        writeIndex(list)
    }

    fun saveExportJpg(bitmap: Bitmap, baseName: String): File {
        val file = File(exportsDir, "$baseName.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
        }
        return file
    }

    fun saveExportPdf(bytes: ByteArray, baseName: String): File {
        val file = File(exportsDir, "$baseName.pdf")
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    private fun writeIndex(list: List<PhotoItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("fileName", item.fileName)
                    put("sizeLabel", item.sizeLabel)
                    put("widthMm", item.widthMm.toDouble())
                    put("heightMm", item.heightMm.toDouble())
                    put("category", item.category)
                    put("createdAt", item.createdAt)
                }
            )
        }
        indexFile.writeText(arr.toString())
    }
}
