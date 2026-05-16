package com.shilpakala.app.ui.studio

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shilpakala.app.data.BrandBackground
import com.shilpakala.app.data.BrandComposer
import com.shilpakala.app.data.BrandOptions
import com.shilpakala.app.data.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class StudioUiState(
    val step: Int = 0,
    val sourceBitmap: Bitmap? = null,
    val productName: String = "",
    val woodType: String = "",
    val price: String = "",
    val artisanName: String = "",
    val background: BrandBackground = BrandBackground.CREAM,
    val resultBitmap: Bitmap? = null,
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GalleryRepository(application)

    private val _ui = MutableStateFlow(StudioUiState())
    val ui: StateFlow<StudioUiState> = _ui.asStateFlow()

    @Volatile
    var imageCapture: ImageCapture? = null

    fun clearMessage() {
        _ui.update { it.copy(message = null, error = null) }
    }

    fun goToStep(step: Int) {
        _ui.update { it.copy(step = step.coerceIn(0, 2)) }
    }

    fun setSourceBitmap(bitmap: Bitmap?) {
        _ui.update { it.copy(sourceBitmap = bitmap) }
    }

    fun setProductName(v: String) = _ui.update { it.copy(productName = v) }
    fun setWoodType(v: String) = _ui.update { it.copy(woodType = v) }
    fun setPrice(v: String) = _ui.update { it.copy(price = v) }
    fun setArtisanName(v: String) = _ui.update { it.copy(artisanName = v) }
    fun setBackground(bg: BrandBackground) = _ui.update { it.copy(background = bg) }

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
            }.onSuccess { bmp ->
                if (bmp != null) {
                    val old = _ui.value.sourceBitmap
                    _ui.update { it.copy(sourceBitmap = bmp, step = 1, error = null) }
                    old?.recycle()
                } else {
                    _ui.update { it.copy(error = "Could not read image") }
                }
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Could not read image") }
            }
        }
    }

    fun takePhoto(executor: java.util.concurrent.Executor) {
        val capture = imageCapture ?: return
        val file = File.createTempFile("shilpakala_cap", ".jpg", getApplication<Application>().cacheDir)
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            opts,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    file.delete()
                    if (bmp != null) {
                        val old = _ui.value.sourceBitmap
                        _ui.update { it.copy(sourceBitmap = bmp, step = 1, error = null) }
                        old?.recycle()
                    } else {
                        _ui.update { it.copy(error = "Capture failed") }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    file.delete()
                    _ui.update { it.copy(error = exception.message ?: "Capture failed") }
                }
            }
        )
    }

    fun generateHeritagePhoto() {
        val src = _ui.value.sourceBitmap ?: return
        val opts = BrandOptions(
            productName = _ui.value.productName,
            woodType = _ui.value.woodType,
            price = _ui.value.price,
            artisanName = _ui.value.artisanName,
            background = _ui.value.background
        )
        _ui.update { it.copy(isGenerating = true, error = null) }
        viewModelScope.launch {
            runCatching {
                BrandComposer.compose(src, opts)
            }.onSuccess { out ->
                _ui.value.resultBitmap?.recycle()
                _ui.update {
                    it.copy(resultBitmap = out, isGenerating = false, step = 2)
                }
            }.onFailure { e ->
                _ui.update {
                    it.copy(isGenerating = false, error = e.message ?: "Compose failed")
                }
            }
        }
    }

    fun saveToAppGallery() {
        val bmp = _ui.value.resultBitmap ?: return
        val name = _ui.value.productName.ifBlank { "piece" }
        val price = _ui.value.price
        _ui.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                repository.saveBrandedJpeg(bmp, name, price)
            }.onSuccess {
                _ui.update {
                    it.copy(isSaving = false, message = "Saved to My Gallery")
                }
            }.onFailure { e ->
                _ui.update {
                    it.copy(isSaving = false, error = e.message ?: "Save failed")
                }
            }
        }
    }

    fun startOver() {
        val src = _ui.value.sourceBitmap
        val res = _ui.value.resultBitmap
        _ui.value = StudioUiState()
        src?.recycle()
        res?.recycle()
    }

    override fun onCleared() {
        val src = _ui.value.sourceBitmap
        val res = _ui.value.resultBitmap
        super.onCleared()
        src?.recycle()
        res?.recycle()
    }
}
