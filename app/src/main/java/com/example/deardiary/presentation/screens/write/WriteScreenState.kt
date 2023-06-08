package com.example.deardiary.presentation.screens.write

import com.example.deardiary.domain.model.Mood
import com.example.deardiary.util.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

data class WriteScreenState(
    val diaryId: String? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val initialDate: RealmInstant? = null,
    val date: RealmInstant = Instant.now().toRealmInstant()
)