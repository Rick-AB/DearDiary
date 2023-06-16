package com.example.auth.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import com.example.auth.AuthScreen
import com.example.auth.AuthScreenEvent
import com.example.auth.AuthScreenSideEffect
import com.example.auth.AuthViewModel
import com.example.util.Destinations
import com.example.util.observeWithLifecycle
import com.kiwi.navigationcompose.typed.composable
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.authentication(navigateToHome: () -> Unit) {
    composable<Destinations.Authentication> {
        val viewModel: AuthViewModel = hiltViewModel()
        val oneTapSignInState = rememberOneTapSignInState()
        val loadingState = viewModel.loadingState.value || oneTapSignInState.opened
        val messageBarState = rememberMessageBarState()

        viewModel.sideEffect.observeWithLifecycle {
            when (it) {
                is AuthScreenSideEffect.Error -> messageBarState.addError(it.exception)
                is AuthScreenSideEffect.LoginFailed -> messageBarState.addError(Exception(it.message))
                is AuthScreenSideEffect.LoginSuccess -> {
                    messageBarState.addSuccess(it.message)
                    delay(1500.milliseconds)
                    navigateToHome()
                }
            }
        }

        AuthScreen(
            loading = loadingState,
            oneTapSignInState = oneTapSignInState,
            messageBarState = messageBarState,
            onClick = { oneTapSignInState.open() },
            onTokenReceived = { viewModel.onEvent(AuthScreenEvent.Login(it)) },
            onDialogDismissed = { messageBarState.addError(Exception(it)) }
        )
    }
}