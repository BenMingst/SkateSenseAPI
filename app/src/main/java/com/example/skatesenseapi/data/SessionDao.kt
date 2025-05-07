package com.example.skatesenseapi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE startTime BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getSessionsBetweenDates(start: Date, end: Date): Flow<List<SessionEntity>>

    @Query("SELECT SUM(distance) FROM sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getTotalDistanceForDateRange(start: Date, end: Date): Float?

    @Query("SELECT AVG(averageSpeed) FROM sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getAverageSpeedForDateRange(start: Date, end: Date): Float?

    @Query("SELECT MAX(maxSpeed) FROM sessions")
    suspend fun getAllTimeMaxSpeed(): Float?

    @Query("SELECT SUM(duration) FROM sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getTotalDurationForDateRange(start: Date, end: Date): Long?

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE startTime < :date")
    suspend fun deleteSessionsOlderThan(date: Date)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SessionEntity?

    @Query("SELECT AVG(distance) FROM sessions")
    suspend fun getAverageDistance(): Float

    @Query("SELECT AVG(averageSpeed) FROM sessions")
    suspend fun getAverageSpeed(): Float
}