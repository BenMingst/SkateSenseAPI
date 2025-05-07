package com.example.skatesenseapi.network

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val current: Current,
    val daily: Daily
)

data class Current(
    val temperature_2m: Float,
    val relative_humidity_2m: Int,
    val wind_speed_10m: Float,
    val wind_direction_10m: Int
)

data class Daily(
    val uv_index_max: List<Float>
)

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "uv_index_max",
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m",
        @Query("timezone") timezone: String = "America/New_York",
        @Query("wind_speed_unit") windSpeedUnit: String = "mph",
        @Query("temperature_unit") temperatureUnit: String = "fahrenheit",
        @Query("precipitation_unit") precipitationUnit: String = "inch"
    ): WeatherResponse
}