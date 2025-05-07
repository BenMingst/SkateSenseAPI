package com.example.skatesenseapi.data

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    private val gson = Gson()

    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

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

    suspend fun deleteSession(session: SessionEntity) {
        sessionDao.deleteSession(session)
    }

    suspend fun getSessionById(id: Long): SessionEntity? {
        return sessionDao.getSessionById(id)
    }

    fun getSessionsBetweenDates(start: Date, end: Date): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsBetweenDates(start, end)
    }

    suspend fun getTotalDistanceForDateRange(start: Date, end: Date): Float {
        return sessionDao.getTotalDistanceForDateRange(start, end) ?: 0f
    }

    suspend fun getAverageSpeedForDateRange(start: Date, end: Date): Float {
        return sessionDao.getAverageSpeedForDateRange(start, end) ?: 0f
    }

    suspend fun getAllTimeMaxSpeed(): Float {
        return sessionDao.getAllTimeMaxSpeed() ?: 0f
    }

    suspend fun getLatestSession(): SessionEntity? {
        return sessionDao.getLatestSession()
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