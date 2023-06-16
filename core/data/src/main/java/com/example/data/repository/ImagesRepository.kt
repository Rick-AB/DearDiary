package com.example.data.repository

import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.deardiary.data.database.entity.ImageToUpload

interface ImagesRepository {

    suspend fun getImagesToUpload(): List<ImageToUpload>
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)
    suspend fun removeImageToUpload(id: Int)

    suspend fun getImagesToDelete(): List<ImageToDelete>
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)
    suspend fun removeImageToDelete(id: Int)
}