package com.example.skatesenseapi.navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NavigationUpdate(
    val nextTurn: String? = null,
    val terrain: String? = null,
    val distanceToNextTurn: Float? = null
)

class NavigationProvider(private val context: Context) {
    private val _navigationUpdates = MutableStateFlow<NavigationUpdate?>(null)
    val navigationUpdates: StateFlow<NavigationUpdate?> = _navigationUpdates
    
    private val elevationProvider = ElevationProvider()
    private var lastLocation: Location? = null

    fun isOsmAndInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("net.osmand", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun startNavigation(latitude: Double, longitude: Double) {
        val osmandIntent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("net.osmand")
            data = android.net.Uri.parse(
                "osmand.api://navigate" +
                "?lat=$latitude" +
                "&lon=$longitude" +
                "&profile=bicycle" +
                "&force=true"
            )
        }
        
        context.startActivity(osmandIntent)
        startListeningForUpdates()
    }

    fun updateLocation(location: Location) {
        elevationProvider.addElevationPoint(location)
        lastLocation = location
        
        val terrain = elevationProvider.getTerrainType()
        updateNavigationState(terrain = terrain)
    }

    private fun startListeningForUpdates() {
        // TODO: Implement OSMAnd API connection
        // For now, we'll simulate navigation updates
        updateNavigationState(
            nextTurn = "Turn right onto Mountain Trail",
            terrain = lastLocation?.let { elevationProvider.getTerrainType() } ?: "Level",
            distanceToNextTurn = 0.2f
        )
    }

    private fun updateNavigationState(
        nextTurn: String? = _navigationUpdates.value?.nextTurn,
        terrain: String? = _navigationUpdates.value?.terrain,
        distanceToNextTurn: Float? = _navigationUpdates.value?.distanceToNextTurn
    ) {
        _navigationUpdates.value = NavigationUpdate(
            nextTurn = nextTurn,
            terrain = terrain,
            distanceToNextTurn = distanceToNextTurn
        )
    }

    fun stopNavigation() {
        _navigationUpdates.value = null
        elevationProvider.clear()
        lastLocation = null
    }
}