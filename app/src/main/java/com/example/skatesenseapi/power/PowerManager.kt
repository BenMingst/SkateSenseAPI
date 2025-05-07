package com.example.skatesenseapi.power

import android.content.Context
import android.location.Location
import android.os.PowerManager as AndroidPowerManager
import kotlin.math.abs

class PowerManager(context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as AndroidPowerManager
    private val wakeLock = powerManager.newWakeLock(
        AndroidPowerManager.PARTIAL_WAKE_LOCK,
        "SkateSense::TrackingWakeLock"
    )
    
    private var lastLocation: Location? = null
    private var significantMovement = true
    private var updateInterval = DEFAULT_UPDATE_INTERVAL
    
    companion object {
        private const val DEFAULT_UPDATE_INTERVAL = 250L // 250ms
        private const val STATIONARY_UPDATE_INTERVAL = 1000L // 1 second
        private const val MOVEMENT_THRESHOLD = 1.0 // meters
        private const val BATTERY_THRESHOLD = 15 // percent
    }

    fun startTracking() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(8 * 60 * 60 * 1000L) // 8 hours max
        }
    }

    fun stopTracking() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        resetState()
    }

    fun getOptimalUpdateInterval(location: Location?, batteryLevel: Int): Long {
        // Adjust interval based on movement
        updateInterval = when {
            batteryLevel <= BATTERY_THRESHOLD -> STATIONARY_UPDATE_INTERVAL * 2
            !hasSignificantMovement(location) -> STATIONARY_UPDATE_INTERVAL
            else -> DEFAULT_UPDATE_INTERVAL
        }
        
        lastLocation = location
        return updateInterval
    }

    private fun hasSignificantMovement(location: Location?): Boolean {
        if (location == null || lastLocation == null) return true
        
        val distance = lastLocation?.distanceTo(location) ?: 0f
        significantMovement = distance > MOVEMENT_THRESHOLD
        return significantMovement
    }

    fun shouldUpdateWeather(batteryLevel: Int): Boolean {
        // Update weather less frequently when battery is low
        return batteryLevel > BATTERY_THRESHOLD
    }

    fun isLowBattery(batteryLevel: Int): Boolean {
        return batteryLevel <= BATTERY_THRESHOLD
    }

    private fun resetState() {
        lastLocation = null
        significantMovement = true
        updateInterval = DEFAULT_UPDATE_INTERVAL
    }
}