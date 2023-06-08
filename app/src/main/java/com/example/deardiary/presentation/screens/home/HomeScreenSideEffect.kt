package com.example.deardiary.presentation.screens.home

sealed interface HomeScreenSideEffect {
    object SignedOut : HomeScreenSideEffect
}