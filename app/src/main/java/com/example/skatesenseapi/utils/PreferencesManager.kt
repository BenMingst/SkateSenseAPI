package com.example.skatesenseapi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _lastConnectedDevice = MutableStateFlow<String?>(
        prefs.getString(KEY_LAST_DEVICE, null)
    )
    val lastConnectedDevice: StateFlow<String?> = _lastConnectedDevice

    private val _enabledSensors = MutableStateFlow(loadEnabledSensors())
    val enabledSensors: StateFlow<Map<String, Boolean>> = _enabledSensors

    fun setLastConnectedDevice(address: String) {
        prefs.edit {
            putString(KEY_LAST_DEVICE, address)
        }
        _lastConnectedDevice.value = address
    }

    fun setSensorEnabled(sensorName: String, enabled: Boolean) {
        prefs.edit {
            putBoolean("sensor_$sensorName", enabled)
        }
        _enabledSensors.value = _enabledSensors.value.toMutableMap().apply {
            put(sensorName, enabled)
        }
    }

    private fun loadEnabledSensors(): Map<String, Boolean> {
        return DEFAULT_SENSORS.associateWith { sensor ->
            prefs.getBoolean("sensor_$sensor", true)
        }
    }

    fun clearLastConnectedDevice() {
        prefs.edit {
            remove(KEY_LAST_DEVICE)
        }
        _lastConnectedDevice.value = null
    }

    companion object {
        private const val PREFS_NAME = "skate_sense_prefs"
        private const val KEY_LAST_DEVICE = "last_connected_device"

        private val DEFAULT_SENSORS = listOf(
            "temperature",
            "humidity",
            "wind",
            "uvIndex",
            "velocity",
            "averageSpeed",
            "distance",
            "nextTurn",
            "terrain",
            "waterAlarm",
            "electrolyteAlarm",
            "foodAlarm",
            "coordinates"
        )
    }
}