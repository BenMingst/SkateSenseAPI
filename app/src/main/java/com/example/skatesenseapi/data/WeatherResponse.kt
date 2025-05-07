package com.example.skatesenseapi.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("current") val current: CurrentWeather?
) : Parcelable

@Parcelize
data class CurrentWeather(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature2m: Double,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Int,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double,
    @SerializedName("wind_direction_10m") val windDirection10m: Double,
    @SerializedName("rain") val rain: Double
) : Parcelable