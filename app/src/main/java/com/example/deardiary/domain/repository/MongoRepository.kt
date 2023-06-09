package com.example.deardiary.domain.repository

import com.example.deardiary.domain.model.Diary
import com.example.deardiary.domain.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime

typealias DiaryResult = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureRealm()
    fun getDiaries(): Flow<DiaryResult>
    fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<DiaryResult>
    suspend fun getDiary(diaryId: ObjectId): RequestState<Diary>
    suspend fun upsertDiary(diary: Diary): RequestState<Diary>
    suspend fun deleteDiary(diaryId: ObjectId): RequestState<Diary>
    suspend fun deleteAllDiaries(): RequestState<Unit>
}