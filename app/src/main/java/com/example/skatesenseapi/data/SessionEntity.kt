package com.example.skatesenseapi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Date,
    val endTime: Date,
    val duration: Long, // in milliseconds
    val distance: Float, // in miles
    val averageSpeed: Float, // in mph
    val maxSpeed: Float, // in mph
    val totalElevationGain: Float, // in feet
    val averageTemperature: Float, // in Fahrenheit
    val averageHumidity: Int, // percentage
    val averageUvIndex: Float,
    val waterAlarmCount: Int,
    val electrolyteAlarmCount: Int,
    val foodAlarmCount: Int,
    val route: String // GeoJSON string of route coordinates
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}