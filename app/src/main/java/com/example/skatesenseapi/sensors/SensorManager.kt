package com.example.skatesenseapi.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import android.os.BatteryManager
import android.os.SystemClock
import com.example.skatesenseapi.data.*
import com.example.skatesenseapi.navigation.NavigationProvider
import com.example.skatesenseapi.network.WeatherService
import com.example.skatesenseapi.notifications.SkateSenseNotificationManager
import com.example.skatesenseapi.power.PowerManager
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.*
import com.example.skatesenseapi.health.HealthMonitor

sealed class SensorError {
    object LocationDisabled : SensorError()
    object WeatherApiError : SensorError()
    object NavigationError : SensorError()
}

class SensorManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    private val navigationProvider = NavigationProvider(context)
    private val healthMonitor = HealthMonitor()
    private val notificationManager = SkateSenseNotificationManager(context)
    private val powerManager = PowerManager(context)
    
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData
    
    private val _errors = MutableStateFlow<SensorError?>(null)
    val errors: StateFlow<SensorError?> = _errors
    
    private var startTime: Long = 0
    private var lastLocation: Location? = null
    private val locationList = mutableListOf<Location>()
    private val speedReadings = mutableListOf<Float>()
    private var currentUpdateInterval = PowerManager.DEFAULT_UPDATE_INTERVAL
    
    private val weatherService = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleLocationUpdate(location)
            }
        }
    }
    
    init {
        scope.launch {
            navigationProvider.navigationUpdates.collect { update ->
                updateSensorData { current ->
                    current.copy(
                        nextTurn = update?.nextTurn,
                        terrain = update?.terrain
                    )
                }
            }
        }
    }
    
    fun startTracking() {
        startTime = SystemClock.elapsedRealtime()
        powerManager.startTracking()
        requestLocationUpdates()
        startSensorListening()
        startWeatherUpdates()
    }
    
    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopSensorListening()
        navigationProvider.stopNavigation()
        powerManager.stopTracking()
        scope.coroutineContext.cancelChildren()
        reset()
    }
    
    private fun requestLocationUpdates() {
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, currentUpdateInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(currentUpdateInterval)
                .build()
                
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            // Handle permission not granted
            _errors.value = SensorError.LocationDisabled
        }
    }
    
    private fun handleLocationUpdate(location: Location) {
        try {
            // Update interval based on battery and movement
            val batteryLevel = getBatteryLevel()
            val newInterval = powerManager.getOptimalUpdateInterval(location, batteryLevel)
            
            if (newInterval != currentUpdateInterval) {
                currentUpdateInterval = newInterval
                // Restart location updates with new interval
                requestLocationUpdates()
            }
            
            lastLocation?.let { last ->
                val speed = location.speed * 2.237f // Convert m/s to mph
                speedReadings.add(speed)
                
                locationList.add(location)
                
                // Update navigation with new location for terrain detection
                try {
                    navigationProvider.updateLocation(location)
                } catch (e: Exception) {
                    _errors.value = SensorError.NavigationError
                }
                
                updateSensorData { current ->
                    current.copy(
                        velocity = VelocityData(
                            speed = speed,
                            direction = getCardinalDirection(location.bearing)
                        ),
                        averageSpeed = speedReadings.average().toFloat(),
                        distance = locationList.calculateTotalDistance(),
                        coordinates = Coordinates(location.latitude, location.longitude)
                    )
                }
                
                // Update navigation if coordinates have changed significantly
                if (location.distanceTo(last) > 10) { // Update if moved more than 10 meters
                    try {
                        navigationProvider.startNavigation(location.latitude, location.longitude)
                        _errors.value = null // Clear navigation error if successful
                    } catch (e: Exception) {
                        _errors.value = SensorError.NavigationError
                    }
                }
            }
            lastLocation = location
        } catch (e: Exception) {
            _errors.value = SensorError.LocationDisabled
        }
    }
    
    private fun startWeatherUpdates() {
        scope.launch {
            while (isActive) {
                try {
                    val batteryLevel = getBatteryLevel()
                    if (powerManager.shouldUpdateWeather(batteryLevel)) {
                        lastLocation?.let { location ->
                            val weather = weatherService.getWeatherData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                            
                            updateSensorData { current ->
                                current.copy(
                                    temperature = weather.current.temperature_2m,
                                    humidity = weather.current.relative_humidity_2m,
                                    wind = WindData(
                                        speed = weather.current.wind_speed_10m,
                                        direction = getCardinalDirection(weather.current.wind_direction_10m.toFloat())
                                    ),
                                    uvIndex = weather.daily.uv_index_max.firstOrNull() ?: 0f
                                )
                            }
                            
                            _errors.value = null // Clear weather error if successful
                            checkAlarms()
                        }
                    }
                } catch (e: Exception) {
                    _errors.value = SensorError.WeatherApiError
                }
                
                // Adjust weather update frequency based on battery level
                val delayTime = if (powerManager.isLowBattery(getBatteryLevel())) {
                    TimeUnit.MINUTES.toMillis(15) // 15 minutes when battery is low
                } else {
                    TimeUnit.MINUTES.toMillis(5)  // 5 minutes normally
                }
                delay(delayTime)
            }
        }
    }
    
    private fun checkAlarms() {
        val currentData = _sensorData.value ?: return
        val activityDuration = ((System.currentTimeMillis() - startTime) / 60000).toInt() // Convert to minutes

        // Calculate water interval based on current conditions
        val waterInterval = currentData.temperature?.let { temp ->
            healthMonitor.calculateWaterInterval(
                temperature = temp,
                humidity = currentData.humidity ?: 50,
                uvIndex = currentData.uvIndex ?: 5f,
                activityLevel = currentData.averageSpeed ?: 0f
            )
        } ?: 30

        // Calculate electrolyte interval
        val electrolyteInterval = healthMonitor.calculateElectrolyteInterval(
            waterInterval = waterInterval,
            activityDuration = activityDuration
        )

        // Calculate food interval
        val foodInterval = healthMonitor.calculateFoodInterval(
            averageSpeed = currentData.averageSpeed ?: 0f,
            activityDuration = activityDuration
        )

        // Check and trigger alarms with notifications
        val waterAlarm = healthMonitor.shouldTriggerWaterAlarm(waterInterval)
        val electrolyteAlarm = healthMonitor.shouldTriggerElectrolyteAlarm(electrolyteInterval)
        val foodAlarm = healthMonitor.shouldTriggerFoodAlarm(foodInterval)

        if (waterAlarm) {
            notificationManager.showWaterAlert()
        }
        if (electrolyteAlarm) {
            notificationManager.showElectrolyteAlert()
        }
        if (foodAlarm) {
            notificationManager.showFoodAlert()
        }

        // Update sensor data with alarm states
        updateSensorData { current ->
            current.copy(
                waterAlarm = waterAlarm,
                electrolyteAlarm = electrolyteAlarm,
                foodAlarm = foodAlarm
            )
        }
    }
    
    private fun getCardinalDirection(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((bearing + 22.5) % 360 / 45).toInt()
        return directions[index]
    }
    
    private fun List<Location>.calculateTotalDistance(): Float {
        var totalDistance = 0f
        for (i in 1 until size) {
            totalDistance += get(i-1).distanceTo(get(i))
        }
        return totalDistance * 0.000621371f // Convert meters to miles
    }
    
    private fun updateSensorData(update: (SensorData) -> SensorData) {
        _sensorData.value = update(_sensorData.value ?: SensorData())
    }
    
    private fun reset() {
        startTime = 0
        lastLocation = null
        locationList.clear()
        speedReadings.clear()
        _sensorData.value = null
        healthMonitor.reset()
        notificationManager.clearAllNotifications()
    }
    
    private fun startSensorListening() {
        // Additional sensor listeners can be added here if needed
    }
    
    private fun stopSensorListening() {
        // Clean up sensor listeners
    }
    
    private fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100 // Assume full battery if we can't get the level
        }
    }
}