package com.example.skatesenseapi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skatesenseapi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isStreaming = false
    private var holdStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.startStopButton.setOnClickListener {
            if (!isStreaming) {
                isStreaming = true
                binding.startStopButton.text = "Hold to Stop"
                // Start streaming logic here
            } else {
                holdStartTime = System.currentTimeMillis()
                binding.startStopButton.setOnTouchListener { _, event ->
                    val duration = System.currentTimeMillis() - holdStartTime
                    if (duration > 5000) {
                        isStreaming = false
                        binding.startStopButton.text = "Start"
                        // Stop streaming logic here
                        true
                    } else false
                }
            }
        }
    }
}
