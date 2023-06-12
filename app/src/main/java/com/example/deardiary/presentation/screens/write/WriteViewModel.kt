package com.example.deardiary.presentation.screens.write

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deardiary.DearDiaryApp
import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.deardiary.data.database.entity.ImageToUpload
import com.example.deardiary.data.repository.MongoRepo
import com.example.deardiary.domain.model.Diary
import com.example.deardiary.domain.model.GalleryImage
import com.example.deardiary.domain.model.GalleryState
import com.example.deardiary.domain.model.Mood
import com.example.deardiary.domain.model.RequestState
import com.example.deardiary.domain.repository.ImagesRepository
import com.example.deardiary.util.fetchImagesFromFirebase
import com.example.deardiary.util.toInstant
import com.example.deardiary.util.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val application: DearDiaryApp,
    private val imagesRepository: ImagesRepository
) : ViewModel() {

    val galleryState = GalleryState(emptyList())
    var uiState by mutableStateOf(WriteScreenState())

    private val sideEffectChannel = Channel<WriteScreenSideEffect>()
    val sideEffect = sideEffectChannel.receiveAsFlow()

    fun onEvent(event: WriteScreenEvent) {
        when (event) {
            is WriteScreenEvent.OnForeGround -> getDiary(event.diaryId)
            is WriteScreenEvent.OnTitleChanged -> updateTitle(event.title)
            is WriteScreenEvent.OnDescriptionChanged -> updateDescription(event.description)
            is WriteScreenEvent.OnMoodChanged -> updateMood(event.mood)
            is WriteScreenEvent.OnDateChanged -> updateDate(event.localDate)
            is WriteScreenEvent.OnTimeChanged -> updateTime(event.localTime)
            is WriteScreenEvent.OnImagesSelected -> updateImagesToFirebase(event.uris)
            is WriteScreenEvent.OnRemoveImage -> removeImage(event.galleryImage)
            WriteScreenEvent.ResetDate -> resetDate()
            WriteScreenEvent.OnSaveClick -> saveDiary()
            WriteScreenEvent.OnDelete -> deleteDiary()
        }
    }

    private fun saveDiary() {
        viewModelScope.launch {
            val diary = Diary().apply {
                if (uiState.diaryId != null) {
                    _id = ObjectId.invoke(uiState.diaryId!!)
                }
                title = uiState.title
                description = uiState.description
                mood = uiState.mood.name
                images = galleryState.images.map { it.remoteImagePath }.toRealmList()
                date = uiState.date
            }

            val result = MongoRepo.upsertDiary(diary)
            if (result is RequestState.Success) {
                uploadImages()
                deleteImages(galleryState.imagesToDelete.map(GalleryImage::remoteImagePath))
                sideEffectChannel.send(WriteScreenSideEffect.SaveSuccess)
            } else if (result is RequestState.Error) {
                sideEffectChannel.send(WriteScreenSideEffect.SaveFailure(result.error.message.toString()))
            }
        }
    }

    private fun deleteDiary() {
        viewModelScope.launch {
            val result = MongoRepo.deleteDiary(ObjectId.invoke(uiState.diaryId!!))
            if (result is RequestState.Success) {
                deleteImages(uiState.images)
                sideEffectChannel.send(WriteScreenSideEffect.DeleteSuccess)
            } else if (result is RequestState.Error) {
                sideEffectChannel.send(WriteScreenSideEffect.DeleteFailure(result.error.message.toString()))
            }
        }
    }

    private fun getDiary(diaryId: String) {
        viewModelScope.launch {
            val result = MongoRepo.getDiary(ObjectId.invoke(diaryId))
            if (result is RequestState.Success) {
                val data = result.data
                uiState = uiState.copy(
                    diaryId = diaryId,
                    title = data.title,
                    description = data.description,
                    mood = Mood.valueOf(data.mood),
                    images = data.images,
                    initialDate = data.date,
                    date = data.date
                )

                fetchImagesFromFirebase(
                    imagesPath = data.images.toList(),
                    onImageDownloaded = { downloadedImageUri, imagePath ->
                        galleryState.addImage(
                            GalleryImage(
                                imageUri = downloadedImageUri,
                                remoteImagePath = imagePath
                            )
                        )
                    }
                )
            }
        }
    }

    private fun updateTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    private fun updateDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun updateMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    private fun updateImagesToFirebase(images: List<Uri>) {
        val galleryImages = images.map { uri ->
            val remotePath = buildRemotePath(uri)
            GalleryImage(uri, remotePath)
        }
        galleryState.addImages(galleryImages)
    }

    private fun updateDate(localDate: LocalDate) {
        val zoneId = ZoneId.systemDefault()
        val currentDiaryTime = uiState.date.toInstant().atZone(zoneId).toLocalTime()
        val zonedDateTime = ZonedDateTime.of(localDate, currentDiaryTime, zoneId)
        uiState = uiState.copy(date = zonedDateTime.toInstant().toRealmInstant())
    }

    private fun updateTime(localTime: LocalTime) {
        val zoneId = ZoneId.systemDefault()
        val currentDiaryDate = uiState.date.toInstant().atZone(zoneId).toLocalDate()
        val zonedDateTime = ZonedDateTime.of(currentDiaryDate, localTime, zoneId)
        uiState = uiState.copy(date = zonedDateTime.toInstant().toRealmInstant())
    }

    private fun resetDate() {
        uiState = if (uiState.initialDate != null) uiState.copy(date = uiState.initialDate!!)
        else uiState.copy(date = Instant.now().toRealmInstant())
    }

    private fun removeImage(galleryImage: GalleryImage) {
        galleryState.removeImage(galleryImage)
    }

    private fun buildRemotePath(uri: Uri): String {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        val mediaType =
            DearDiaryApp.appContext.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
        val fileName = "${uri.lastPathSegment}-${System.currentTimeMillis()}.$mediaType"
        return "images/$firebaseUserId/$fileName"
    }

    private fun uploadImages() {
        val storage = FirebaseStorage.getInstance().reference
        application.applicationScope.launch {
            galleryState.images.forEach { galleryImage ->
                val remotePath = galleryImage.remoteImagePath
                val imageUri = galleryImage.imageUri
                storage.child(remotePath)
                    .putFile(imageUri)
                    .addOnProgressListener {
                        val sessionUri = it.uploadSessionUri
                        if (sessionUri != null) {
                            launch {
                                val imageToUpload = ImageToUpload(
                                    remotePath = remotePath,
                                    imageUri = imageUri.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                                imagesRepository.addImageToUpload(imageToUpload)
                            }
                        }
                    }
            }
        }
    }

    private fun deleteImages(images: List<String>) {
        uiState.diaryId ?: return

        val storage = FirebaseStorage.getInstance().reference
        images.forEach { path ->
            storage.child(path)
                .delete()
                .addOnFailureListener {
                    application.applicationScope.launch {
                        imagesRepository.addImageToDelete(ImageToDelete(remotePath = path))
                    }
                }
        }
    }
}