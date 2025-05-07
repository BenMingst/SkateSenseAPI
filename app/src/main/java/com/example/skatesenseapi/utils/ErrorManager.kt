package com.example.skatesenseapi.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AppError {
    data class BluetoothError(val message: String) : AppError()
    data class LocationError(val message: String) : AppError()
    data class WeatherError(val message: String) : AppError()
    data class NavigationError(val message: String) : AppError()
    data class SensorError(val message: String) : AppError()
}

class ErrorManager {
    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError: StateFlow<AppError?> = _currentError

    fun setError(error: AppError) {
        _currentError.value = error
    }

    fun clearError() {
        _currentError.value = null
    }

    companion object {
        const val ERROR_BLUETOOTH_DISCONNECTED = "Bluetooth connection lost"
        const val ERROR_BLUETOOTH_PERMISSION = "Bluetooth permission required"
        const val ERROR_LOCATION_PERMISSION = "Location permission required"
        const val ERROR_LOCATION_DISABLED = "Location services disabled"
        const val ERROR_WEATHER_API = "Weather data unavailable"
        const val ERROR_OSMAND_NOT_INSTALLED = "OSMAnd app not installed"
        const val ERROR_SENSOR_UNAVAILABLE = "Required sensor unavailable"
    }
}