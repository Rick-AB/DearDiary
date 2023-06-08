package com.example.deardiary.presentation.screens.authentication

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deardiary.R
import com.example.deardiary.util.Constants.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    val loadingState = mutableStateOf(false)

    private val _sideEffect = Channel<AuthScreenSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()


    fun onEvent(event: AuthScreenEvent) {
        when (event) {
            is AuthScreenEvent.Login -> loginWithMongoAtlas(event.tokenId)
        }
    }

    private fun loginWithMongoAtlas(tokenId: String) {
        loadingState.value = true
        viewModelScope.launch {
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

            loadingState.value = false
        }
    }

    private fun getString(@StringRes stringRes: Int) = application.getString(stringRes)
}