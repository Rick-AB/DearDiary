package com.example.util

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface Destinations : Destination {
    @Serializable
    object Authentication : Destinations

    @Serializable
    object Home : Destinations

    @Serializable
    data class Write(val diaryId: String? = null) : Destinations
}