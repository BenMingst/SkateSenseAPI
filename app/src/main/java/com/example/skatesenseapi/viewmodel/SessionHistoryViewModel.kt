package com.example.skatesenseapi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.skatesenseapi.data.SessionEntity
import com.example.skatesenseapi.data.SessionRepository
import com.example.skatesenseapi.data.SessionStatistics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class SessionHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SessionRepository(application)
    private val _timeRange = MutableStateFlow(TimeRange.MONTH)
    
    val sessions = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _statistics = MutableStateFlow<SessionStatistics?>(null)
    val statistics: StateFlow<SessionStatistics?> = _statistics

    init {
        viewModelScope.launch {
            _timeRange.collectLatest { range ->
                updateStatistics(range)
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    private suspend fun updateStatistics(range: TimeRange) {
        val (start, end) = getDateRange(range)
        _statistics.value = repository.getStatisticsForTimeRange(start, end)
    }

    private fun getDateRange(range: TimeRange): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val end = calendar.time
        
        calendar.add(when (range) {
            TimeRange.WEEK -> Calendar.WEEK_OF_YEAR
            TimeRange.MONTH -> Calendar.MONTH
            TimeRange.YEAR -> Calendar.YEAR
        }, -1)
        
        val start = calendar.time
        return start to end
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
            updateStatistics(_timeRange.value)
        }
    }

    enum class TimeRange {
        WEEK, MONTH, YEAR
    }
}