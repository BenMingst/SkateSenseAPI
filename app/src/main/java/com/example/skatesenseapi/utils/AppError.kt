package com.example.skatesenseapi.utils

sealed class AppError {
    object None : AppError()
    data class BluetoothError(val message: String) : AppError()
    data class LocationError(val message: String) : AppError()
    data class WeatherError(val message: String) : AppError()
    data class NavigationError(val message: String) : AppError()
    data class SensorError(val message: String) : AppError()
}