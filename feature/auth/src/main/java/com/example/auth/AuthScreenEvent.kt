package com.example.auth

internal sealed interface AuthScreenEvent {
    data class Login(val tokenId: String) : AuthScreenEvent
}