package com.example.deardiary.di

import android.content.Context
import androidx.room.Room
import com.example.data.repository.ImagesRepo
import com.example.data.repository.ImagesRepository
import com.example.deardiary.data.database.ImageDatabase
import com.example.util.Constants.IMAGE_DB_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ImageDatabase {
        return Room.databaseBuilder(context, ImageDatabase::class.java, IMAGE_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideImagesRepo(database: ImageDatabase): ImagesRepository {
        return ImagesRepo(database)
    }
}