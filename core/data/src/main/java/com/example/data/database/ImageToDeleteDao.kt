package com.example.deardiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.deardiary.data.database.entity.ImageToDelete

@Dao
interface ImageToDeleteDao {

    @Query("SELECT * FROM ImageToDelete ORDER BY id ASC")
    suspend fun getImagesToDelete(): List<ImageToDelete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImage(imageToDelete: ImageToDelete)

    @Query("DELETE FROM ImageToDelete WHERE id=:imageId")
    suspend fun removeImage(imageId: Int)
}