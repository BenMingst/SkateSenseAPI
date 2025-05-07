package com.skatesense.api.data

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.math.roundToInt

class SessionRepository(context: Context) {
    private val sessionDao = AppDatabase.getDatabase(context).sessionDao()
    private val gson = Gson()

    suspend fun createSession(
        startTime: Date,
        endTime: Date,
        distance: Float,
        averageSpeed: Float,
        maxSpeed: Float,
        totalElevationGain: Float,
        averageTemperature: Float,
        averageHumidity: Int,
        averageUvIndex: Float,
        waterAlarmCount: Int,
        electrolyteAlarmCount: Int,
        foodAlarmCount: Int,
        routeLocations: List<Location>
    ): Long {
        val session = SessionEntity(
            startTime = startTime,
            endTime = endTime,
            duration = endTime.time - startTime.time,
            distance = distance,
            averageSpeed = averageSpeed,
            maxSpeed = maxSpeed,
            totalElevationGain = totalElevationGain,
            averageTemperature = averageTemperature,
            averageHumidity = averageHumidity,
            averageUvIndex = averageUvIndex,
            waterAlarmCount = waterAlarmCount,
            electrolyteAlarmCount = electrolyteAlarmCount,
            foodAlarmCount = foodAlarmCount,
            route = createGeoJson(routeLocations)
        )
        return sessionDao.insertSession(session)
    }

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsBetweenDates(start: Date, end: Date): Flow<List<SessionEntity>> =
        sessionDao.getSessionsBetweenDates(start, end)

    suspend fun getSessionById(id: Long): SessionEntity? = sessionDao.getSessionById(id)

    suspend fun deleteSession(session: SessionEntity) = sessionDao.deleteSession(session)

    suspend fun deleteOldSessions(olderThan: Date) = sessionDao.deleteSessionsOlderThan(olderThan)

    suspend fun getStatisticsForTimeRange(start: Date, end: Date): SessionStatistics {
        return SessionStatistics(
            totalDistance = sessionDao.getTotalDistanceForDateRange(start, end) ?: 0f,
            averageSpeed = sessionDao.getAverageSpeedForDateRange(start, end) ?: 0f,
            allTimeMaxSpeed = sessionDao.getAllTimeMaxSpeed() ?: 0f,
            totalDuration = sessionDao.getTotalDurationForDateRange(start, end) ?: 0L,
            sessionCount = sessionDao.getSessionCount()
        )
    }

    private fun createGeoJson(locations: List<Location>): String {
        val coordinates = locations.map { location ->
            listOf(location.longitude, location.latitude, location.altitude)
        }

        val geoJson = mapOf(
            "type" to "LineString",
            "coordinates" to coordinates
        )

        return gson.toJson(geoJson)
    }
}

data class SessionStatistics(
    val totalDistance: Float,
    val averageSpeed: Float,
    val allTimeMaxSpeed: Float,
    val totalDuration: Long,
    val sessionCount: Int
)