package com.shilpakala.app.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shilpakala.app.data.GalleryItem
import com.shilpakala.app.data.GalleryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GalleryRepository(application)

    val items = repository.items.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun delete(item: GalleryItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
