package com.skatesense.api

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skatesense.api.databinding.ActivitySettingsBinding


// Settings Activity
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val settingsItems = listOf(
        "Temperature", "Humidity", "Wind", "UV Index",
        "Velocity", "Direction", "Average Speed", "Distance",
        "Duration", "Next Turn", "Terrain", "Water Alarm",
        "Electrolyte Alarm", "Food Alarm", "Coordinates"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.settingsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = SettingsAdapter(settingsItems)
        recyclerView.adapter = adapter

        binding.settingsToolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
