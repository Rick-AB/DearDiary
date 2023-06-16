package com.example.auth

sealed interface AuthScreenEvent {
    data class Login(val tokenId: String) : AuthScreenEvent
}