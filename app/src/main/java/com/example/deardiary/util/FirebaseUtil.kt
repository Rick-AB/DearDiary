package com.example.deardiary.util

import android.net.Uri
import androidx.core.net.toUri
import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.deardiary.data.database.entity.ImageToUpload
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun fetchImagesFromFirebase(
    imagesPath: List<String>,
    onImageDownloaded: (Uri, String) -> Unit,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val storage = FirebaseStorage.getInstance().reference
    coroutineScope.launch {
        if (imagesPath.isNotEmpty()) {
            imagesPath.forEach { path ->
                if (path.trim().isNotEmpty()) {
                    launch {
                        val uri = storage.child(path).downloadUrl.await()
                        onImageDownloaded(uri, path)
                    }
                }
            }
        }
    }
}

fun retryImageUploadToFirebase(
    imageToUpload: ImageToUpload,
    coroutineScope: CoroutineScope,
    onSuccess: suspend () -> Unit
) {
    coroutineScope.launch {
        val storage = FirebaseStorage.getInstance().reference
        val result = storage.child(imageToUpload.remotePath)
            .putFile(
                imageToUpload.imageUri.toUri(),
                storageMetadata { },
                imageToUpload.sessionUri.toUri()
            ).await()

        if (result.task.isSuccessful) onSuccess()
    }
}

fun retryImageToDeleteFromFirebase(
    imageToDelete: ImageToDelete,
    coroutineScope: CoroutineScope,
    onSuccess: suspend () -> Unit
) {
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToDelete.remotePath).delete()
        .addOnSuccessListener { coroutineScope.launch { onSuccess() } }
}