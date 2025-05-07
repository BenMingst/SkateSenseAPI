package com.example.skatesenseapi.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCoordinatesList(value: String?): List<Coordinates>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Coordinates>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun coordinatesListToString(list: List<Coordinates>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }
}