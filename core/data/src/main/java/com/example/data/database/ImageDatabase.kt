package com.example.deardiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.deardiary.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao
}