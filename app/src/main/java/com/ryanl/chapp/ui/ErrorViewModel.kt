package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.ErrorReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ErrorViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ErrorReporter.ErrorState())
    val uiState: StateFlow<ErrorReporter.ErrorState> = _uiState.asStateFlow()

    fun enterView() {
        viewModelScope.launch {
            ErrorReporter.subscribe(::errorChanged)
        }
    }

    fun exitView() {
        viewModelScope.launch {
            ErrorReporter.unsubscribe(::errorChanged)
        }
    }

    private fun errorChanged(e: ErrorReporter.ErrorTypes) {
        _uiState.update { currentState ->
            currentState.copy(
                serverError = ErrorReporter.getErrorState().serverError,
                internetError = ErrorReporter.getErrorState().internetError,
                authError = ErrorReporter.getErrorState().authError
            )
        }
    }
}