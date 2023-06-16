package com.example.deardiary.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageToUpload(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remotePath: String,
    val imageUri: String,
    val sessionUri: String
)
