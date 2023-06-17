package com.example.home

import java.time.LocalDate

internal sealed interface HomeScreenEvent {
    object SignOut : HomeScreenEvent
    object DeleteAllDiaries : HomeScreenEvent
    data class OnDateSelected(val localDate: LocalDate?) : HomeScreenEvent
}