package com.example.deardiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.deardiary.data.database.entity.ImageToUpload

@Dao
interface ImageToUploadDao {

    @Query("SELECT * FROM ImageToUpload ORDER BY id ASC")
    suspend fun getImagesToUpload(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImage(imageToUpload: ImageToUpload)

    @Query("DELETE FROM ImageToUpload WHERE id=:imageId")
    suspend fun removeImage(imageId: Int)
}