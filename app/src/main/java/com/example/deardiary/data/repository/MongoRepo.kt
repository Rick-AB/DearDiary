package com.example.deardiary.data.repository

import com.example.deardiary.domain.model.Diary
import com.example.deardiary.domain.model.RequestState
import com.example.deardiary.domain.repository.DiaryResult
import com.example.deardiary.domain.repository.MongoRepository
import com.example.deardiary.util.Constants.APP_ID
import com.example.deardiary.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MongoRepo : MongoRepository {
    private val user = App.create(APP_ID).currentUser
    private lateinit var realm: Realm

    init {
        configureRealm()
    }

    override fun configureRealm() {
        user ?: return

        val config = SyncConfiguration.Builder(user, setOf(Diary::class))
            .initialSubscriptions { sub ->
                add(
                    query = sub.query<Diary>(query = "ownerId == $0", user.id),
                    name = "User's diaries"
                )
            }
            .log(LogLevel.ALL)
            .build()

        realm = Realm.open(config)
    }

    override fun getDiaries(): Flow<DiaryResult> {
        return if (user == null) {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        } else {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort("date", Sort.DESCENDING)
                    .asFlow()
                    .map { res ->
                        RequestState.Success(
                            data = res.list.groupBy { diary ->
                                diary.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )

                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        }
    }

    override fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<DiaryResult> {
        return if (user == null) {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        } else {
            try {
                val earliestDate = RealmInstant.from(
                    LocalDateTime.of(
                        zonedDateTime.toLocalDate().plusDays(1),
                        LocalTime.MIDNIGHT
                    ).toEpochSecond(zonedDateTime.offset), 0
                )
                val latestDate = RealmInstant.from(
                    LocalDateTime.of(
                        zonedDateTime.toLocalDate(),
                        LocalTime.MIDNIGHT
                    ).toEpochSecond(zonedDateTime.offset), 0
                )
                realm.query<Diary>(
                    query = "ownerId == $0 AND date < $1 AND date > $2",
                    user.id,
                    earliestDate,
                    latestDate
                )
                    .sort("date", Sort.DESCENDING)
                    .asFlow()
                    .map { res ->
                        RequestState.Success(
                            data = res.list.groupBy { diary ->
                                diary.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )

                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        }
    }

    override suspend fun getDiary(diaryId: ObjectId): RequestState<Diary> {
        return withContext(Dispatchers.IO) {
            if (user == null) {
                RequestState.Error(UserNotAuthenticatedException())
            } else {
                try {
                    val result = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                    RequestState.Success(result)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        }
    }

    override suspend fun upsertDiary(diary: Diary): RequestState<Diary> {
        return withContext(Dispatchers.IO) {
            if (user == null) {
                RequestState.Error(UserNotAuthenticatedException())
            } else {
                try {
                    val result = realm.write {
                        copyToRealm(
                            diary.apply { ownerId = user.id },
                            UpdatePolicy.ALL
                        )
                    }
                    RequestState.Success(result)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        }
    }

    override suspend fun deleteDiary(diaryId: ObjectId): RequestState<Diary> {
        return withContext(Dispatchers.IO) {
            if (user == null) {
                RequestState.Error(UserNotAuthenticatedException())
            } else {
                realm.write {
                    val diary = query<Diary>("_id == $0 AND ownerId == $1", diaryId, user.id)
                        .first().find()

                    if (diary != null) {
                        try {
                            delete(diary)
                            RequestState.Success(diary)
                        } catch (e: Exception) {
                            RequestState.Error(e)
                        }
                    } else {
                        RequestState.Error(Exception("Diary does not exist."))
                    }
                }
            }
        }
    }

    override suspend fun deleteAllDiaries(): RequestState<Unit> {
        return withContext(Dispatchers.IO) {
            if (user == null) {
                RequestState.Error(UserNotAuthenticatedException())
            } else {
                realm.write {
                    val diaries = query<Diary>("ownerId == $0", user.id).find()
                    try {
                        delete(diaries)
                        RequestState.Success(Unit)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                }
            }
        }
    }
}


private class UserNotAuthenticatedException : Exception("User not logged in")