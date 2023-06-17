package com.example.write

import com.example.util.model.Mood
import com.example.util.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

internal data class WriteScreenState(
    val diaryId: String? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val initialDate: RealmInstant? = null,
    val images: List<String> = emptyList(),
    val date: RealmInstant = Instant.now().toRealmInstant()
)