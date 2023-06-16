package com.example.data.repository

import com.example.deardiary.data.database.ImageDatabase
import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.deardiary.data.database.entity.ImageToUpload

class ImagesRepo(database: ImageDatabase) : ImagesRepository {

    private val imageToUploadDao = database.imageToUploadDao()
    private val imageToDeleteDao = database.imageToDeleteDao()

    override suspend fun getImagesToUpload(): List<ImageToUpload> {
        return imageToUploadDao.getImagesToUpload()
    }

    override suspend fun addImageToUpload(imageToUpload: ImageToUpload) {
        imageToUploadDao.addImage(imageToUpload)
    }

    override suspend fun removeImageToUpload(id: Int) {
        imageToUploadDao.removeImage(id)
    }

    override suspend fun getImagesToDelete(): List<ImageToDelete> {
        return imageToDeleteDao.getImagesToDelete()
    }

    override suspend fun addImageToDelete(imageToDelete: ImageToDelete) {
        imageToDeleteDao.addImage(imageToDelete)
    }

    override suspend fun removeImageToDelete(id: Int) {
        imageToDeleteDao.removeImage(id)
    }
}