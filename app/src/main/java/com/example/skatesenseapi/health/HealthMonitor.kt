package com.example.skatesenseapi.health

import kotlin.math.max
import kotlin.math.min

class HealthMonitor {
    private var lastWaterAlarmTime = 0L
    private var lastElectrolyteAlarmTime = 0L
    private var lastFoodAlarmTime = 0L

    /**
     * Calculate water alarm interval based on environmental conditions and activity
     * @param temperature Temperature in Fahrenheit
     * @param humidity Relative humidity percentage
     * @param uvIndex UV index value
     * @param activityLevel Average speed in mph
     * @return Interval in minutes between water reminders
     */
    fun calculateWaterInterval(
        temperature: Float,
        humidity: Int,
        uvIndex: Float,
        activityLevel: Float
    ): Int {
        // Base interval starts at 30 minutes
        var interval = 30f

        // Temperature adjustment (reduce interval as temperature increases)
        val tempFactor = when {
            temperature > 95f -> 0.5f  // Very hot
            temperature > 85f -> 0.7f  // Hot
            temperature > 75f -> 0.85f // Warm
            else -> 1.0f              // Comfortable or cool
        }
        
        // Humidity adjustment (reduce interval with higher humidity)
        val humidityFactor = when {
            humidity > 80 -> 0.7f  // Very humid
            humidity > 60 -> 0.85f // Humid
            else -> 1.0f          // Moderate or low humidity
        }
        
        // UV index adjustment
        val uvFactor = when {
            uvIndex > 8f -> 0.7f  // Very high UV
            uvIndex > 5f -> 0.85f // High UV
            else -> 1.0f         // Moderate or low UV
        }
        
        // Activity level adjustment (reduce interval with higher activity)
        val activityFactor = when {
            activityLevel > 15f -> 0.6f  // High intensity
            activityLevel > 10f -> 0.75f // Moderate intensity
            activityLevel > 5f -> 0.9f   // Light intensity
            else -> 1.0f                 // Very light intensity
        }
        
        // Apply all factors
        interval *= tempFactor * humidityFactor * uvFactor * activityFactor
        
        // Ensure interval stays within reasonable bounds (10-45 minutes)
        return min(45, max(10, interval.toInt()))
    }

    /**
     * Calculate electrolyte alarm interval based on water consumption and activity
     * @param waterInterval The calculated water interval
     * @param activityDuration Duration of activity in minutes
     * @return Interval in minutes between electrolyte reminders
     */
    fun calculateElectrolyteInterval(waterInterval: Int, activityDuration: Int): Int {
        // Base electrolyte interval is 2x water interval
        var interval = waterInterval * 2
        
        // Adjust based on activity duration
        interval = when {
            activityDuration > 120 -> (interval * 0.7).toInt() // Long duration
            activityDuration > 60 -> (interval * 0.85).toInt() // Moderate duration
            else -> interval
        }
        
        // Ensure interval stays within reasonable bounds (30-90 minutes)
        return min(90, max(30, interval))
    }

    /**
     * Calculate food/snack alarm interval based on activity intensity and duration
     * @param averageSpeed Average speed in mph
     * @param activityDuration Duration of activity in minutes
     * @return Interval in minutes between food reminders
     */
    fun calculateFoodInterval(averageSpeed: Float, activityDuration: Int): Int {
        // Base interval of 60 minutes
        var interval = 60
        
        // Adjust for intensity (speed)
        val intensityFactor = when {
            averageSpeed > 15f -> 0.7f  // High intensity
            averageSpeed > 10f -> 0.85f // Moderate intensity
            else -> 1.0f               // Light intensity
        }
        
        // Adjust for duration
        val durationFactor = when {
            activityDuration > 180 -> 0.7f  // Very long activity
            activityDuration > 120 -> 0.85f // Long activity
            activityDuration > 60 -> 0.95f  // Moderate duration
            else -> 1.0f                   // Short duration
        }
        
        interval = (interval * intensityFactor * durationFactor).toInt()
        
        // Ensure interval stays within reasonable bounds (30-120 minutes)
        return min(120, max(30, interval))
    }

    fun shouldTriggerWaterAlarm(interval: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWaterAlarmTime >= interval * 60 * 1000) {
            lastWaterAlarmTime = currentTime
            return true
        }
        return false
    }

    fun shouldTriggerElectrolyteAlarm(interval: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastElectrolyteAlarmTime >= interval * 60 * 1000) {
            lastElectrolyteAlarmTime = currentTime
            return true
        }
        return false
    }

    fun shouldTriggerFoodAlarm(interval: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFoodAlarmTime >= interval * 60 * 1000) {
            lastFoodAlarmTime = currentTime
            return true
        }
        return false
    }

    fun reset() {
        lastWaterAlarmTime = 0
        lastElectrolyteAlarmTime = 0
        lastFoodAlarmTime = 0
    }
}