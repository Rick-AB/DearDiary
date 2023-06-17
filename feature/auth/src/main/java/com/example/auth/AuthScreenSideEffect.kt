package com.example.auth

internal sealed interface AuthScreenSideEffect {
    data class LoginSuccess(val message: String) : AuthScreenSideEffect
    data class LoginFailed(val message: String) : AuthScreenSideEffect
    data class Error(val exception: Exception) : AuthScreenSideEffect
}