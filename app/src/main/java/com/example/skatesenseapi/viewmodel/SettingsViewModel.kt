package com.example.skatesenseapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skatesenseapi.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferenceManager
) : ViewModel() {

    private val _units = MutableStateFlow(preferences.getUnits())
    val units: StateFlow<PreferenceManager.Units> = _units.asStateFlow()

    private val _notifyWater = MutableStateFlow(preferences.getNotifyWater())
    val notifyWater: StateFlow<Boolean> = _notifyWater.asStateFlow()

    private val _notifyElectrolyte = MutableStateFlow(preferences.getNotifyElectrolyte())
    val notifyElectrolyte: StateFlow<Boolean> = _notifyElectrolyte.asStateFlow()

    private val _notifyFood = MutableStateFlow(preferences.getNotifyFood())
    val notifyFood: StateFlow<Boolean> = _notifyFood.asStateFlow()

    fun setUnits(units: PreferenceManager.Units) {
        viewModelScope.launch {
            preferences.setUnits(units)
            _units.value = units
        }
    }

    fun setNotifyWater(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotifyWater(enabled)
            _notifyWater.value = enabled
        }
    }

    fun setNotifyElectrolyte(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotifyElectrolyte(enabled)
            _notifyElectrolyte.value = enabled
        }
    }

    fun setNotifyFood(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotifyFood(enabled)
            _notifyFood.value = enabled
        }
    }
}