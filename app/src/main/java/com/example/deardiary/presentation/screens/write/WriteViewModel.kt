package com.example.deardiary.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deardiary.data.repository.MongoRepo
import com.example.deardiary.domain.model.Diary
import com.example.deardiary.domain.model.Mood
import com.example.deardiary.domain.model.RequestState
import com.example.deardiary.util.toInstant
import com.example.deardiary.util.toRealmInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
) : ViewModel() {

    var uiState by mutableStateOf(WriteScreenState())

    private val sideEffectChannel = Channel<WriteScreenSideEffect>()
    val sideEffect = sideEffectChannel.receiveAsFlow()

    fun onEvent(event: WriteScreenEvent) {
        when (event) {
            is WriteScreenEvent.OnForeGround -> getDiary(event.diaryId)
            is WriteScreenEvent.OnTitleChanged -> updateTitle(event.title)
            is WriteScreenEvent.OnDescriptionChanged -> updateDescription(event.description)
            is WriteScreenEvent.OnMoodChanged -> updateMood(event.mood)
            is WriteScreenEvent.OnDateChanged -> updateDate(event.localDate)
            is WriteScreenEvent.OnTimeChanged -> updateTime(event.localTime)
            WriteScreenEvent.ResetDate -> resetDate()
            WriteScreenEvent.OnSaveClick -> saveDiary()
        }
    }

    private fun saveDiary() {
        viewModelScope.launch {
            val diary = Diary().apply {
                if (uiState.diaryId != null) {
                    _id = ObjectId.invoke(uiState.diaryId!!)
                }
                title = uiState.title
                description = uiState.description
                mood = uiState.mood.name
                date = uiState.date
            }

            when (val result = MongoRepo.upsertDiary(diary)) {
                is RequestState.Error ->
                    sideEffectChannel.send(WriteScreenSideEffect.SaveFailure(result.error.message.toString()))

                is RequestState.Success -> sideEffectChannel.send(WriteScreenSideEffect.SaveSuccess)
                else -> {}
            }
        }
    }

    private fun getDiary(diaryId: String) {
        viewModelScope.launch {
            val result = MongoRepo.getDiary(ObjectId.invoke(diaryId))
            if (result is RequestState.Success) {
                val data = result.data
                uiState = uiState.copy(
                    diaryId = diaryId,
                    title = data.title,
                    description = data.description,
                    mood = Mood.valueOf(data.mood),
                    initialDate = data.date,
                    date = data.date
                )
            }
        }
    }

    private fun updateTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    private fun updateDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun updateMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    private fun updateDate(localDate: LocalDate) {
        val zoneId = ZoneId.systemDefault()
        val currentDiaryTime = uiState.date.toInstant().atZone(zoneId).toLocalTime()
        val zonedDateTime = ZonedDateTime.of(localDate, currentDiaryTime, zoneId)
        uiState = uiState.copy(date = zonedDateTime.toInstant().toRealmInstant())
    }

    private fun updateTime(localTime: LocalTime) {
        val zoneId = ZoneId.systemDefault()
        val currentDiaryDate = uiState.date.toInstant().atZone(zoneId).toLocalDate()
        val zonedDateTime = ZonedDateTime.of(currentDiaryDate, localTime, zoneId)
        uiState = uiState.copy(date = zonedDateTime.toInstant().toRealmInstant())
    }

    private fun resetDate() {
        uiState = if (uiState.initialDate != null) uiState.copy(date = uiState.initialDate!!)
        else uiState.copy(date = Instant.now().toRealmInstant())
    }
}