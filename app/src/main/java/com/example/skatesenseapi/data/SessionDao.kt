package com.skatesense.api.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SkatingSession): Long

    @Query("SELECT * FROM skating_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SkatingSession>>

    @Query("SELECT * FROM skating_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SkatingSession?

    @Query("SELECT * FROM skating_sessions WHERE startTime BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getSessionsBetweenDates(start: Date, end: Date): Flow<List<SkatingSession>>

    @Query("SELECT SUM(distance) FROM skating_sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getTotalDistanceForDateRange(start: Date, end: Date): Float?

    @Query("SELECT AVG(averageSpeed) FROM skating_sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getAverageSpeedForDateRange(start: Date, end: Date): Float?

    @Query("SELECT MAX(maxSpeed) FROM skating_sessions")
    suspend fun getAllTimeMaxSpeed(): Float?

    @Query("SELECT SUM(duration) FROM skating_sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getTotalDurationForDateRange(start: Date, end: Date): Long?

    @Delete
    suspend fun deleteSession(session: SkatingSession)

    @Query("DELETE FROM skating_sessions WHERE startTime < :date")
    suspend fun deleteSessionsOlderThan(date: Date)

    @Query("SELECT COUNT(*) FROM skating_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT * FROM skating_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SkatingSession?

    @Query("SELECT AVG(distance) FROM skating_sessions")
    suspend fun getAverageDistance(): Float

    @Query("SELECT AVG(averageSpeed) FROM skating_sessions")
    suspend fun getAverageSpeed(): Float
}