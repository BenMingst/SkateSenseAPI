package com.example.skatesenseapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skatesenseapi.databinding.ActivitySessionHistoryBinding
import com.example.skatesenseapi.viewmodel.SessionHistoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SessionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionHistoryBinding
    private val viewModel: SessionHistoryViewModel by viewModels()
    private lateinit var adapter: SessionHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Session History"
    }

    private fun setupRecyclerView() {
        adapter = SessionHistoryAdapter { session ->
            // Navigate to session details
            SessionDetailsActivity.start(this, session.id)
        }
        
        binding.sessionList.apply {
            layoutManager = LinearLayoutManager(this@SessionHistoryActivity)
            adapter = this@SessionHistoryActivity.adapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.sessions.collectLatest { sessions ->
                adapter.submitList(sessions)
            }
        }

        lifecycleScope.launch {
            viewModel.statistics.collectLatest { stats ->
                stats?.let { updateStatistics(it) }
            }
        }
    }

    private fun updateStatistics(stats: SessionStatistics) {
        binding.apply {
            totalDistanceValue.text = String.format("%.1f mi", stats.totalDistance)
            averageSpeedValue.text = String.format("%.1f mph", stats.averageSpeed)
            maxSpeedValue.text = String.format("%.1f mph", stats.allTimeMaxSpeed)
            totalTimeValue.text = formatDuration(stats.totalDuration)
            sessionCountValue.text = stats.sessionCount.toString()
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%dh %dm", hours, minutes)
    }
}