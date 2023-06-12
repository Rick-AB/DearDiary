package com.example.deardiary.presentation.screens.write

import android.net.Uri
import com.example.deardiary.domain.model.GalleryImage
import com.example.deardiary.domain.model.Mood
import java.time.LocalDate
import java.time.LocalTime

sealed interface WriteScreenEvent {
    object OnSaveClick : WriteScreenEvent
    object ResetDate : WriteScreenEvent
    object OnDelete : WriteScreenEvent
    data class OnForeGround(val diaryId: String) : WriteScreenEvent
    data class OnTitleChanged(val title: String) : WriteScreenEvent
    data class OnDescriptionChanged(val description: String) : WriteScreenEvent
    data class OnMoodChanged(val mood: Mood) : WriteScreenEvent
    data class OnDateChanged(val localDate: LocalDate) : WriteScreenEvent
    data class OnTimeChanged(val localTime: LocalTime) : WriteScreenEvent
    data class OnImagesSelected(val uris: List<Uri>) : WriteScreenEvent
    data class OnRemoveImage(val galleryImage: GalleryImage) : WriteScreenEvent
}