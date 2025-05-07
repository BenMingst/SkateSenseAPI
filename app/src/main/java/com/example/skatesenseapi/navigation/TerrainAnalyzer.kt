package com.example.skatesenseapi.navigation

import android.location.Location
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TerrainAnalyzer {
    private val elevationWindow = mutableListOf<Double>()
    private val windowSize = 5
    private val slopeHistory = mutableListOf<Double>()
    private val maxHistorySize = 10

    fun addElevationPoint(location: Location) {
        // Apply Kalman filter to smooth elevation data
        val filteredElevation = kalmanFilter(location.altitude)
        elevationWindow.add(filteredElevation)
        
        if (elevationWindow.size > windowSize) {
            elevationWindow.removeFirst()
        }

        // Calculate and store slope
        if (elevationWindow.size >= 2) {
            val slope = calculateSlope()
            slopeHistory.add(slope)
            if (slopeHistory.size > maxHistorySize) {
                slopeHistory.removeFirst()
            }
        }
    }

    fun getTerrainDescription(): TerrainInfo {
        if (slopeHistory.isEmpty()) return TerrainInfo("Level", 0.0)

        val avgSlope = slopeHistory.average()
        val slopeVariance = calculateVariance(slopeHistory)
        
        // Determine terrain type based on slope and variance
        val (terrainType, grade) = when {
            abs(avgSlope) < 2.0 -> "Level" to avgSlope
            avgSlope > 0 -> {
                when {
                    avgSlope > 15.0 -> "Very Steep Uphill" to avgSlope
                    avgSlope > 8.0 -> "Steep Uphill" to avgSlope
                    else -> "Uphill" to avgSlope
                }
            }
            else -> {
                when {
                    abs(avgSlope) > 15.0 -> "Very Steep Downhill" to avgSlope
                    abs(avgSlope) > 8.0 -> "Steep Downhill" to avgSlope
                    else -> "Downhill" to avgSlope
                }
            }
        }

        // Add terrain roughness if significant variance detected
        val roughnessDescription = when {
            slopeVariance > 5.0 -> "Rough "
            slopeVariance > 2.0 -> "Moderate "
            else -> ""
        }

        return TerrainInfo(roughnessDescription + terrainType, grade)
    }

    private fun kalmanFilter(measurement: Double): Double {
        // Simple Kalman filter implementation for elevation smoothing
        val previousEstimate = elevationWindow.lastOrNull() ?: measurement
        val errorEstimate = 1.0
        val measurementError = 2.0
        
        val gain = errorEstimate / (errorEstimate + measurementError)
        return previousEstimate + gain * (measurement - previousEstimate)
    }

    private fun calculateSlope(): Double {
        val elevationChange = elevationWindow.last() - elevationWindow.first()
        val distance = (windowSize - 1) * 1.0 // Assuming constant distance between samples
        return (elevationChange / distance) * 100 // Convert to percentage grade
    }

    private fun calculateVariance(values: List<Double>): Double {
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average()
    }

    fun clear() {
        elevationWindow.clear()
        slopeHistory.clear()
    }

    data class TerrainInfo(
        val description: String,
        val grade: Double
    )
}