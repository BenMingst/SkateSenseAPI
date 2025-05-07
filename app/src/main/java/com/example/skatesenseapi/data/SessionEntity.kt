package com.example.skatesenseapi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Date,
    val endTime: Date,
    val duration: Long,
    val distance: Float,
    val averageSpeed: Float,
    val maxSpeed: Float,
    val totalElevationGain: Float,
    val averageTemperature: Float,
    val averageHumidity: Int,
    val averageUvIndex: Float,
    val waterAlarmCount: Int,
    val electrolyteAlarmCount: Int,
    val foodAlarmCount: Int,
    val route: String // GeoJSON string representation of the route
)