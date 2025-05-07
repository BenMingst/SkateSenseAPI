package com.example.skatesenseapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skatesenseapi.databinding.ActivitySettingsBinding
import com.example.skatesenseapi.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeSettings()
    }

    private fun setupToolbar() {
        binding.settingsToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        settingsAdapter = SettingsAdapter { sensorName ->
            viewModel.toggleSensor(sensorName)
        }
        binding.settingsList.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = settingsAdapter
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            viewModel.enabledSensors.collect { settings ->
                val items = settings.map { (name, enabled) ->
                    SettingItem(
                        name = formatSensorName(name),
                        key = name,
                        enabled = enabled
                    )
                }
                settingsAdapter.submitList(items)
            }
        }
    }

    private fun formatSensorName(key: String): String {
        return key.replace(Regex("([A-Z])"), " $1")
            .replaceFirstChar { it.uppercase() }
    }
}