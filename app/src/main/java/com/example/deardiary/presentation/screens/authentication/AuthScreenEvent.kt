package com.example.deardiary.presentation.screens.authentication

sealed interface AuthScreenEvent {
    data class Login(val tokenId: String) : AuthScreenEvent
}