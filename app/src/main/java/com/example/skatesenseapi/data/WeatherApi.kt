package com.example.skatesenseapi.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "uv_index_max",
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,rain,uv_index",
        @Query("wind_speed_unit") windSpeedUnit: String = "mph",
        @Query("temperature_unit") temperatureUnit: String = "fahrenheit",
        @Query("precipitation_unit") precipitationUnit: String = "inch"
    ): Call<WeatherResponse>
}