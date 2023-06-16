package com.example.home

sealed interface HomeScreenSideEffect {
    object SignedOut : HomeScreenSideEffect
    object DiariesDeleted : HomeScreenSideEffect
    data class Error(val message: String) : HomeScreenSideEffect
}