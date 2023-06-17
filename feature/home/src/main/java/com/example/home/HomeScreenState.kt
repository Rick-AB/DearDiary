package com.example.home

import com.example.util.model.Diary
import java.time.LocalDate

internal sealed interface HomeScreenState {
    data class DataLoaded(val items: Map<LocalDate, List<Diary>>) : HomeScreenState
    data class Error(val message: String) : HomeScreenState
    object Loading : HomeScreenState
}