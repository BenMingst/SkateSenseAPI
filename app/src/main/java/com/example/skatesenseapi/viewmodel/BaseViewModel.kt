package com.example.skatesenseapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skatesenseapi.utils.AppError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<AppError>()
    val errors: SharedFlow<AppError> = _errors

    protected fun emitError(error: AppError) {
        viewModelScope.launch {
            _errors.emit(error)
        }
    }

    fun clearError() {
        viewModelScope.launch {
            _errors.emit(AppError.None)
        }
    }
}