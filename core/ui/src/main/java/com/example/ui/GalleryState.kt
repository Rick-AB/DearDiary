package com.example.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

@Composable
fun rememberGalleryState(images: List<GalleryImage> = emptyList()): GalleryState {
    return remember { GalleryState(images) }
}

class GalleryState(images: List<GalleryImage>) {
    val images: SnapshotStateList<GalleryImage> = images.toMutableStateList()
    val imagesToDelete = mutableStateListOf<GalleryImage>()

    fun addImage(galleryImage: GalleryImage) {
        images.add(galleryImage)
    }

    fun addImages(images: List<GalleryImage>) {
        this.images.addAll(images)
    }

    fun removeImage(galleryImage: GalleryImage) {
        images.remove(galleryImage)
        imagesToDelete.add(galleryImage)
    }


}

data class GalleryImage(
    val imageUri: Uri,
    val remoteImagePath: String = "" // image not uploaded if empty
)