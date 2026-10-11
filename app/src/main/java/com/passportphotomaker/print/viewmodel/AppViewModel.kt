package com.passportphotomaker.print.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.passportphotomaker.print.data.AppSettings
import com.passportphotomaker.print.data.PaperSize
import com.passportphotomaker.print.data.PhotoItem
import com.passportphotomaker.print.data.PhotoRepository
import com.passportphotomaker.print.data.PhotoSize
import com.passportphotomaker.print.data.PhotoSizes
import com.passportphotomaker.print.data.PrintQuality
import com.passportphotomaker.print.data.SettingsRepository
import com.passportphotomaker.print.data.ThemeMode
import com.passportphotomaker.print.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Single shared ViewModel (activity-scoped) that carries state across screens:
 * the photo being edited, the selected ID size, saved photos, and user settings.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PhotoRepository(app)
    private val settingsRepo = SettingsRepository(app)

    val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings())
        .also { flow ->
            viewModelScope.launch {
                settingsRepo.settings.collect { flow.value = it }
            }
        }.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

    /** Raw image picked from the gallery or captured from the camera. */
    private val _sourceUri = MutableStateFlow<Uri?>(null)
    val sourceUri: StateFlow<Uri?> = _sourceUri.asStateFlow()

    /** The edited result (a cache file) produced by the editor. */
    private val _editedFile = MutableStateFlow<File?>(null)
    val editedFile: StateFlow<File?> = _editedFile.asStateFlow()

    private val _selectedSize = MutableStateFlow(PhotoSizes.default)
    val selectedSize: StateFlow<PhotoSize> = _selectedSize.asStateFlow()

    /** Most recent export (JPG or PDF) available for sharing. */
    private val _lastExport = MutableStateFlow<File?>(null)
    val lastExport: StateFlow<File?> = _lastExport.asStateFlow()

    // Editor parameters
    private val _brightness = MutableStateFlow(0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()
    private val _contrast = MutableStateFlow(1f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()
    private val _saturation = MutableStateFlow(1f)
    val saturation: StateFlow<Float> = _saturation.asStateFlow()
    private val _backgroundArgb = MutableStateFlow(Color.WHITE)
    val backgroundArgb: StateFlow<Int> = _backgroundArgb.asStateFlow()

    fun refreshPhotos() {
        viewModelScope.launch {
            _photos.value = withContext(Dispatchers.IO) { repo.loadPhotos() }
        }
    }

    fun setSource(uri: Uri?) { _sourceUri.value = uri }

    fun setEditedFile(file: File?) { _editedFile.value = file }

    fun setSelectedSize(size: PhotoSize) { _selectedSize.value = size }

    fun setBrightness(v: Float) { _brightness.value = v }
    fun setContrast(v: Float) { _contrast.value = v }
    fun setSaturation(v: Float) { _saturation.value = v }
    fun setBackground(argb: Int) { _backgroundArgb.value = argb }

    fun resetAdjustments() {
        _brightness.value = 0f
        _contrast.value = 1f
        _saturation.value = 1f
    }

    /** Persists the edited bitmap to a cache file and exposes it as the working image. */
    suspend fun saveWorking(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val file = File(getApplication<Application>().cacheDir, "working_edit.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 96, it) }
        _editedFile.value = file
        file
    }

    /** Saves the finished photo into the local gallery. */
    suspend fun persistPhoto(bitmap: Bitmap, size: PhotoSize): PhotoItem? =
        withContext(Dispatchers.IO) {
            val item = runCatching { repo.savePhoto(bitmap, size) }.getOrNull()
            _photos.value = repo.loadPhotos()
            item
        }

    fun deletePhoto(item: PhotoItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deletePhoto(item) }
            _photos.value = withContext(Dispatchers.IO) { repo.loadPhotos() }
        }
    }

    suspend fun exportJpg(bitmap: Bitmap, baseName: String): File = withContext(Dispatchers.IO) {
        val file = repo.saveExportJpg(bitmap, baseName)
        _lastExport.value = file
        file
    }

    suspend fun exportPdf(
        photo: Bitmap,
        size: PhotoSize,
        paper: PaperSize,
        dpi: Int,
        pages: Int,
        baseName: String
    ): File = withContext(Dispatchers.IO) {
        val bytes = com.passportphotomaker.print.util.PdfExporter.exportSheetPdf(
            photo = photo,
            photoWmm = size.widthMm,
            photoHmm = size.heightMm,
            paper = paper,
            dpi = dpi,
            pages = pages
        )
        val file = repo.saveExportPdf(bytes, baseName)
        _lastExport.value = file
        file
    }

    fun uriFor(file: File): Uri = repo.uriFor(file)

    fun fileFor(item: PhotoItem): File = repo.photoFile(item)

    /** Builds the final, correctly sized photo bitmap at the given DPI. */
    fun buildFinalPhoto(edited: Bitmap, size: PhotoSize, dpi: Int = 300): Bitmap {
        val w = ImageUtils.mmToPx(size.widthMm, dpi)
        val h = ImageUtils.mmToPx(size.heightMm, dpi)
        return ImageUtils.fitToBox(edited, w, h)
    }

    // ---- Settings ----
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(mode) }
    fun setDefaultPaper(paper: PaperSize) = viewModelScope.launch { settingsRepo.setDefaultPaper(paper) }
    fun setDefaultQuality(q: PrintQuality) = viewModelScope.launch { settingsRepo.setDefaultQuality(q) }
    fun setShowFaceGuide(show: Boolean) = viewModelScope.launch { settingsRepo.setShowFaceGuide(show) }
    fun setOnboardingDone(done: Boolean) = viewModelScope.launch { settingsRepo.setOnboardingDone(done) }
}
