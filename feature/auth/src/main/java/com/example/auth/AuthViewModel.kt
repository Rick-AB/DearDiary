package com.example.auth

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.Constants.APP_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class AuthViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    val loadingState = mutableStateOf(false)

    private val _sideEffect = Channel<AuthScreenSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()


    fun onEvent(event: AuthScreenEvent) {
        when (event) {
            is AuthScreenEvent.Login -> loginWithFirebase(event.tokenId)
        }
    }


    private fun loginWithFirebase(tokenId: String) {
        loadingState.value = true
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(tokenId, null)
            try {
                FirebaseAuth.getInstance().signInWithCredential(credential).await()
                loginWithMongoAtlas(tokenId)
            } catch (e: Exception) {
                _sideEffect.send(AuthScreenSideEffect.Error(e))
            }
            loadingState.value = false
        }
    }

    private suspend fun loginWithMongoAtlas(tokenId: String) {
        try {
            val loginSuccess = withContext(Dispatchers.IO) {
                App.create(APP_ID).login(Credentials.jwt(tokenId)).loggedIn
            }

            if (loginSuccess)
                _sideEffect.send(AuthScreenSideEffect.LoginSuccess(getString(R.string.successfully_authenticated)))
            else
                _sideEffect.send(AuthScreenSideEffect.LoginFailed(getString(R.string.something_went_wrong)))

        } catch (e: Exception) {
            _sideEffect.send(AuthScreenSideEffect.Error(e))
        }
    }

    private fun getString(@StringRes stringRes: Int) = application.getString(stringRes)
}