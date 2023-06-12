package com.example.deardiary.di

import android.content.Context
import com.example.deardiary.DearDiaryApp
import com.example.deardiary.connectivity.ConnectivityObserver
import com.example.deardiary.connectivity.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplication() = DearDiaryApp()

    @Provides
    @Singleton
    fun provideNetworkObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}