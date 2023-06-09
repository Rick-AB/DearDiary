package com.example.deardiary.presentation.screens.write

sealed interface WriteScreenSideEffect {
    object SaveSuccess : WriteScreenSideEffect
    data class SaveFailure(val message: String) : WriteScreenSideEffect
    object DeleteSuccess : WriteScreenSideEffect
    data class DeleteFailure(val message: String) : WriteScreenSideEffect
}