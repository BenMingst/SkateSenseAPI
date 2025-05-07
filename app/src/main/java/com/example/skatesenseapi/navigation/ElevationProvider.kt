package com.example.skatesenseapi.navigation

import android.location.Location
import kotlin.math.abs

class ElevationProvider {
    private val terrainAnalyzer = TerrainAnalyzer()
    private var lastGrade = 0.0

    fun addElevationPoint(location: Location) {
        terrainAnalyzer.addElevationPoint(location)
    }

    fun getTerrainType(): String {
        val terrainInfo = terrainAnalyzer.getTerrainDescription()
        lastGrade = terrainInfo.grade
        return terrainInfo.description
    }

    fun getElevationGradient(): Double {
        return lastGrade
    }

    fun clear() {
        terrainAnalyzer.clear()
        lastGrade = 0.0
    }
}