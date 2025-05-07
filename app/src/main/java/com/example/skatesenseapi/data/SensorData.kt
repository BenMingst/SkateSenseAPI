package com.example.skatesenseapi.data

data class SensorData(
    val temperature: Float? = null,         // Temperature in Fahrenheit
    val humidity: Int? = null,            // Humidity percentage
    val wind: WindData? = null,             // Wind speed and direction
    val uvIndex: Float? = null,             // UV index for the day
    val velocity: VelocityData? = null,     // Current velocity and direction
    val averageSpeed: Float? = null,        // Average speed in mph
    val distance: Float? = null,            // Distance traveled in miles
    val duration: String? = null,                 // Duration in milliseconds
    val nextTurn: String? = null,         // Next turn information
    val terrain: String? = null,            // Terrain description
    val waterAlarm: Boolean = false,        // Water reminder
    val electrolyteAlarm: Boolean = false,  // Electrolyte reminder
    val foodAlarm: Boolean = false,         // Food reminder
    val coordinates: Coordinates? = null    // Current coordinates
)

data class WindData(
    val speed: Float,     // Speed in mph
    val direction: String // Cardinal direction (N, NE, etc.)
)

data class VelocityData(
    val speed: Float,     // Speed in mph
    val direction: String // Cardinal direction
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)