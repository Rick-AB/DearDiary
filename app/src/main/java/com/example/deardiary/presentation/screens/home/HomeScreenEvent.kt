package com.example.deardiary.presentation.screens.home

import java.time.LocalDate

sealed interface HomeScreenEvent {
    object SignOut : HomeScreenEvent
    object DeleteAllDiaries : HomeScreenEvent
    data class OnDateSelected(val localDate: LocalDate?) : HomeScreenEvent
}