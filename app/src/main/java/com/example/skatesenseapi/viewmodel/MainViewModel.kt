package com.example.skatesenseapi.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import com.example.skatesenseapi.bluetooth.BluetoothManager
import com.example.skatesenseapi.data.*
import com.example.skatesenseapi.sensors.SensorManager
import com.example.skatesenseapi.utils.PreferencesManager
import com.example.skatesenseapi.utils.ErrorManager
import com.example.skatesenseapi.utils.AppError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.util.Date
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val errorManager = ErrorManager()
    private val sensorManager = SensorManager(application)
    private val preferencesManager = PreferencesManager(application)
    private val bluetoothManager = BluetoothManager(application) { status ->
        updateBluetoothStatus(status)
        if (status == BluetoothStatus.DISCONNECTED) {
            preferencesManager.clearLastConnectedDevice()
            errorManager.setError(AppError.BluetoothError(ErrorManager.ERROR_BLUETOOTH_DISCONNECTED))
        }
    }
    private val sessionRepository = SessionRepository(application)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    
    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.DISCONNECTED)
    val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus
    
    private var startTime: Long = 0
    private var dataUpdateJob: Job? = null

    // Settings
    val enabledSensors = preferencesManager.enabledSensors

    // Session tracking
    private var sessionStartTime: Date? = null
    private var waterAlarmCount = 0
    private var electrolyteAlarmCount = 0
    private var foodAlarmCount = 0
    private var maxSpeed = 0f

    private val _sessionStats = MutableStateFlow<SessionStatistics?>(null)
    val sessionStats: StateFlow<SessionStatistics?> = _sessionStats

    init {
        viewModelScope.launch {
            sensorManager.sensorData.collect { _ ->
                if (_isRunning.value && bluetoothManager.isConnected.value) {
                    sendJsonData()
                }
            }
        }

        // Try to reconnect to last device
        viewModelScope.launch {
            preferencesManager.lastConnectedDevice.collect { deviceAddress ->
                if (deviceAddress != null && _bluetoothStatus.value == BluetoothStatus.DISCONNECTED) {
                    connectToBluetooth(deviceAddress)
                }
            }
        }

        viewModelScope.launch {
            sensorManager.errors.collect { error ->
                when (error) {
                    is SensorError.LocationDisabled -> 
                        errorManager.setError(AppError.LocationError(ErrorManager.ERROR_LOCATION_DISABLED))
                    is SensorError.WeatherApiError -> 
                        errorManager.setError(AppError.WeatherError(ErrorManager.ERROR_WEATHER_API))
                    is SensorError.NavigationError -> 
                        errorManager.setError(AppError.NavigationError(ErrorManager.ERROR_OSMAND_NOT_INSTALLED))
                    null -> errorManager.clearError()
                }
            }
        }

        viewModelScope.launch {
            loadLatestStatistics()
        }
    }
    
    private fun sendJsonData() {
        val jsonString = getJsonString()
        bluetoothManager.sendData(jsonString)
    }
    
    fun startTracking() {
        if (!_isRunning.value) {
            sessionStartTime = Date()
            waterAlarmCount = 0
            electrolyteAlarmCount = 0
            foodAlarmCount = 0
            maxSpeed = 0f
            startTime = SystemClock.elapsedRealtime()
            sensorManager.startTracking()
            _isRunning.value = true
            startDataUpdates()
        }
    }
    
    private fun startDataUpdates() {
        dataUpdateJob?.cancel()
        dataUpdateJob = viewModelScope.launch {
            while (isActive) {
                sendJsonData()
                delay(250) // Send updates every quarter second
            }
        }
    }
    
    fun stopTracking() {
        if (_isRunning.value) {
            _isRunning.value = false
            sensorManager.stopTracking()
            dataUpdateJob?.cancel()
            saveSession()
            resetSessionData()
        }
    }
    
    fun connectToBluetooth(deviceAddress: String) {
        viewModelScope.launch {
            _bluetoothStatus.value = BluetoothStatus.CONNECTING
            bluetoothManager.connect(deviceAddress)
                .retry(3, 100)
                .useAutoConnect(false)
                .done { 
                    preferencesManager.setLastConnectedDevice(deviceAddress)
                }
                .enqueue()
        }
    }
    
    fun disconnectBluetooth() {
        bluetoothManager.disconnect()
    }
    
    fun updateBluetoothStatus(status: BluetoothStatus) {
        _bluetoothStatus.value = status
    }
    
    fun getJsonString(): String {
        val sensorData = sensorManager.sensorData.value ?: return "{}"
        return JSONObject().apply {
            if (enabledSensors.value["temperature"] == true) {
                put("temperature", "${sensorData.temperature?.roundToInt()}°F")
            }
            if (enabledSensors.value["humidity"] == true) {
                put("humidity", "${sensorData.humidity}%")
            }
            if (enabledSensors.value["wind"] == true && sensorData.wind != null) {
                put("wind", "${sensorData.wind.speed.roundToInt()} mph ${sensorData.wind.direction}")
            }
            if (enabledSensors.value["uvIndex"] == true) {
                put("uvIndex", sensorData.uvIndex)
            }
            if (enabledSensors.value["velocity"] == true && sensorData.velocity != null) {
                put("velocity", "${sensorData.velocity.speed.roundToInt()} mph ${sensorData.velocity.direction}")
            }
            if (enabledSensors.value["averageSpeed"] == true) {
                put("averageSpeed", "${sensorData.averageSpeed?.roundToInt()} mph")
            }
            if (enabledSensors.value["distance"] == true) {
                put("distance", String.format("%.2f mi", sensorData.distance))
            }
            if (enabledSensors.value["nextTurn"] == true) {
                put("nextTurn", sensorData.nextTurn ?: "None")
            }
            if (enabledSensors.value["terrain"] == true) {
                put("terrain", sensorData.terrain ?: "Level")
            }
            if (startTime > 0) {
                put("duration", formatDuration(System.currentTimeMillis() - startTime))
            }
            if (enabledSensors.value["waterAlarm"] == true) {
                put("waterAlarm", sensorData.waterAlarm)
            }
            if (enabledSensors.value["electrolyteAlarm"] == true) {
                put("electrolyteAlarm", sensorData.electrolyteAlarm)
            }
            if (enabledSensors.value["foodAlarm"] == true) {
                put("foodAlarm", sensorData.foodAlarm)
            }
            if (enabledSensors.value["coordinates"] == true && sensorData.coordinates != null) {
                put("coordinates", JSONObject().apply {
                    put("lat", sensorData.coordinates.latitude)
                    put("lon", sensorData.coordinates.longitude)
                })
            }
        }.toString(2)
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
    
    private suspend fun loadLatestStatistics() {
        val calendar = Calendar.getInstance()
        val end = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val start = calendar.time

        _sessionStats.value = sessionRepository.getStatisticsForTimeRange(start, end)
    }

    private fun saveSession() {
        val currentData = sensorManager.sensorData.value ?: return
        val sessionStart = sessionStartTime ?: return

        viewModelScope.launch {
            sessionRepository.createSession(
                startTime = sessionStart,
                endTime = Date(),
                distance = currentData.distance ?: 0f,
                averageSpeed = currentData.averageSpeed ?: 0f,
                maxSpeed = maxSpeed,
                totalElevationGain = currentData.elevationGain ?: 0f,
                averageTemperature = currentData.temperature ?: 0f,
                averageHumidity = currentData.humidity ?: 0,
                averageUvIndex = currentData.uvIndex ?: 0f,
                waterAlarmCount = waterAlarmCount,
                electrolyteAlarmCount = electrolyteAlarmCount,
                foodAlarmCount = foodAlarmCount,
                routeLocations = sensorManager.getLocationHistory()
            )
            loadLatestStatistics()
        }
    }

    private fun resetSessionData() {
        sessionStartTime = null
        waterAlarmCount = 0
        electrolyteAlarmCount = 0
        foodAlarmCount = 0
        maxSpeed = 0f
        startTime = 0
    }

    fun updateSensorData(data: SensorData) {
        // Update max speed if current speed is higher
        data.velocity?.speed?.let { speed ->
            if (speed > maxSpeed) {
                maxSpeed = speed
            }
        }

        // Track alarm triggers
        if (data.waterAlarm) waterAlarmCount++
        if (data.electrolyteAlarm) electrolyteAlarmCount++
        if (data.foodAlarm) foodAlarmCount++
    }

    enum class BluetoothStatus {
        CONNECTED, CONNECTING, DISCONNECTED
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        sensorManager.stopTracking()
        bluetoothManager.disconnect()
    }
}