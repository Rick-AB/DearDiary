package com.example.deardiary.presentation.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deardiary.data.repository.MongoRepo
import com.example.deardiary.domain.model.RequestState
import com.example.deardiary.domain.repository.DiaryResult
import com.example.deardiary.util.Constants.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    val signingOut = mutableStateOf(false)

    val homeState: StateFlow<HomeScreenState> = MongoRepo.getDiaries().map {
        mapRequestStateToHomeState(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeScreenState.Loading)

    private val sideEffectChannel = Channel<HomeScreenSideEffect>()
    val sideEffect = sideEffectChannel.receiveAsFlow()

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            HomeScreenEvent.SignOut -> signOut()
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

    private fun mapRequestStateToHomeState(requestState: DiaryResult): HomeScreenState {
        return when (requestState) {
            is RequestState.Error -> HomeScreenState.Error(requestState.error.message ?: "")
            is RequestState.Success -> HomeScreenState.DataLoaded(requestState.data)
            else -> HomeScreenState.Loading
        }
    }
}