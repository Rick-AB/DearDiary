package com.example.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DiaryResult
import com.example.data.repository.ImagesRepository
import com.example.deardiary.data.database.entity.ImageToDelete
import com.example.data.repository.MongoRepo
import com.example.util.Constants.APP_ID
import com.example.util.connectivity.ConnectivityObserver
import com.example.util.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val app: com.example.util.App,
    private val imagesRepository: ImagesRepository
) : ViewModel() {

    private var networkStatus by mutableStateOf(ConnectivityObserver.Status.Unavailable)

    val signingOut = mutableStateOf(false)
    val deletingDiaries = mutableStateOf(false)
    val dateFilterAsFlow = MutableStateFlow<ZonedDateTime?>(null)

    val homeState: StateFlow<HomeScreenState> = dateFilterAsFlow.flatMapLatest {
        if (it == null) MongoRepo.getDiaries()
        else MongoRepo.getFilteredDiaries(it)
    }.map(::mapRequestStateToHomeState).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        HomeScreenState.Loading
    )

    private val sideEffectChannel = Channel<HomeScreenSideEffect>()
    val sideEffect = sideEffectChannel.receiveAsFlow()

    init {
        observeNetworkStatus()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            HomeScreenEvent.SignOut -> signOut()
            HomeScreenEvent.DeleteAllDiaries -> deleteDiaries()
            is HomeScreenEvent.OnDateSelected -> setDateFilter(event.localDate)
        }
    }

    private fun signOut() {
        signingOut.value = true
        viewModelScope.launch {
            val user = App.create(APP_ID).currentUser ?: return@launch
            user.logOut()
            signingOut.value = false
            delay(1.seconds)
            sideEffectChannel.send(HomeScreenSideEffect.SignedOut)
        }
    }

    private fun deleteDiaries() {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (networkStatus == ConnectivityObserver.Status.Available) {
            deletingDiaries.value = true
            deleteImages(firebaseUserId)
            viewModelScope.launch {
                val result = MongoRepo.deleteAllDiaries()
                if (result is RequestState.Success) {
                    sideEffectChannel.send(HomeScreenSideEffect.DiariesDeleted)
                } else if (result is RequestState.Error) {
                    sideEffectChannel.send(HomeScreenSideEffect.Error(result.error.message.toString()))
                }

                deletingDiaries.value = false
            }
        } else {
            viewModelScope.launch {
                val message =
                    com.example.util.App.appContext.getString(R.string.internet_connection_required)
                sideEffectChannel.send(HomeScreenSideEffect.Error(message))
            }
        }
    }

    private fun setDateFilter(localDate: LocalDate?) {
        if (localDate == null) dateFilterAsFlow.update { null }
        else {
            val zonedDateTime =
                ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.systemDefault())
            dateFilterAsFlow.update { zonedDateTime }
        }
    }

    private fun deleteImages(firebaseUserId: String) {
        val storage = FirebaseStorage.getInstance().reference
        val imagesDirectory = "images/$firebaseUserId"

        storage.child(imagesDirectory).listAll().addOnSuccessListener {
            it.items.map { ref ->
                val imagePath = "$imagesDirectory/${ref.name}"
                deleteImage(storage, imagePath)
            }
        }.addOnFailureListener {
            viewModelScope.launch { sideEffectChannel.send(HomeScreenSideEffect.Error(it.message.toString())) }
        }
    }

    private fun deleteImage(storage: StorageReference, imagePath: String) {
        storage.child(imagePath).delete().addOnFailureListener {
            app.applicationScope.launch {
                val imageToDelete = ImageToDelete(remotePath = imagePath)
                imagesRepository.addImageToDelete(imageToDelete)
            }
        }
    }

    private fun mapRequestStateToHomeState(requestState: DiaryResult): HomeScreenState {
        return when (requestState) {
            is RequestState.Error -> HomeScreenState.Error(requestState.error.message.toString())
            is RequestState.Success -> HomeScreenState.DataLoaded(requestState.data)
            else -> HomeScreenState.Loading
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            connectivityObserver.observe().collect {
                networkStatus = it
            }
        }
    }
}