package com.example.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
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