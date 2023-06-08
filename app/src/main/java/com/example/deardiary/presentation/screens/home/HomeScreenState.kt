package com.example.deardiary.presentation.screens.home

import com.example.deardiary.domain.model.Diary
import java.time.LocalDate

sealed interface HomeScreenState {
    data class DataLoaded(val items: Map<LocalDate, List<Diary>>) : HomeScreenState
    data class Error(val message: String) : HomeScreenState
    object Loading : HomeScreenState
}