package com.example.skatesenseapi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "skating_sessions")
data class SkatingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Date,
    val endTime: Date,
    val distance: Float,
    val averageSpeed: Float,
    val maxSpeed: Float,
    val calories: Int,
    val route: String // JSON string of GPS coordinates
)