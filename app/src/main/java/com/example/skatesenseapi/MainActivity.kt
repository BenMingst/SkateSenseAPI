package com.example.skatesenseapi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.app.ActivityCompat
import com.example.skatesenseapi.data.RetrofitInstance
import com.example.skatesenseapi.data.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

private const val STOPWATCH_BASE = 0




class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTextView: TextView
    private lateinit var speedTextView: TextView // Declared here
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timer: Timer
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var isStopwatchRunning = false
    private lateinit var stopwatch: TextView
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private var elapsedTimeSeconds = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherTextView = findViewById(R.id.weatherTextView)
        speedTextView = findViewById(R.id.speedTextView) // Initialized here
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
        setupLocationUpdates()       // Get the stopwatch element
        stopwatch = findViewById(R.id.stopwatch)

        // Get the play and stop buttons
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)

        // Set up the initial state of the buttons
        updateButtonStates()

        // Set the click listener for the play button
        playButton.setOnClickListener {
            startStopwatch()
        }

        // Set the click listener for the stop button
        stopButton.setOnClickListener {
            stopStopwatch()
        }
    }
    private fun updateButtonStates() {
        playButton.isEnabled = !isStopwatchRunning
        stopButton.isEnabled = isStopwatchRunning
    }

    private fun startStopwatch() {
        if (!isStopwatchRunning) {
            isStopwatchRunning = true
            updateButtonStates()
            startStopwatchTimer()
        }
    }

    private fun stopStopwatch() {
        if (isStopwatchRunning) {
            isStopwatchRunning = false
            elapsedTimeSeconds = 0
            updateButtonStates()
            stopStopwatchTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        requestLocationUpdates()
    }

    private fun startLocationUpdates() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    getLocationAndFetchWeather()
                }
            }
        }, 0, 5000) // Update every 5 seconds (adjust as needed)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeUpdates(locationListener!!)
    }

    private fun getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    fetchWeather(it.latitude, it.longitude)
                }
            }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        val call = RetrofitInstance.api.getForecast(latitude, longitude)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    val current = weatherResponse?.current
                    //Convert to int
                    val windDirectionDegrees = current?.windDirection10m?.toInt() ?: 0

                    // Convert wind direction degrees to cardinal direction
                    val windDirection = getTrackDirection(windDirectionDegrees)

                    val output = "Temperature: ${current?.temperature2m} \n" +
                            "Humidity: ${current?.relativeHumidity2m}%\n" +
                            "Wind: ${current?.windSpeed10m} mph " +
                            "$windDirection"

                    weatherTextView.text = output
                } else {
                    Log.e("MainActivity", "API request failed: ${response.message()}")
                    weatherTextView.text = "API request failed."
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("MainActivity", "API request failed: ${t.message}")
                weatherTextView.text = "API request failed."
            }
        })
    }

    private fun getTrackDirection(degrees: Int): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val index = ((degrees + 22.5) / 45).toInt() % 8
        return directions[index]
    }

    private fun setupLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speed = location.speed
                val speedInMph = speed * 2.23694f // Convert m/s to mph
                val bearing = location.bearing.toDouble()
                val direction = getTrackDirection(bearing.toInt())
                val formattedSpeed = String.format("%.2f", speedInMph)
                val velocity = "$formattedSpeed mph $direction"
                runOnUiThread {
                    speedTextView.text = velocity
                }
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
            return
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            TimeUnit.SECONDS.toMillis(1), // Min time interval between updates (1 second)
            0f, // Min distance between updates (0 meters)
            locationListener!!
        )
    }
    private fun startStopwatchTimer() {
        elapsedTimeSeconds = 0
        stopwatch.text = formatTime(elapsedTimeSeconds)
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (isStopwatchRunning) {
                    elapsedTimeSeconds++
                    stopwatch.text = formatTime(elapsedTimeSeconds)
                    handler.postDelayed(this, 1000) // Update every 1 second
                }
            }
        })
    }

    private fun stopStopwatchTimer() {
        isStopwatchRunning = false
        updateButtonStates()
        stopwatch.text = "00:00:00"
        //stopwatch.base = SystemClock.elapsedRealtime()

    }

    private fun formatTime(elapsedSeconds: Int): String {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isStopwatchRunning", isStopwatchRunning)
    }


}