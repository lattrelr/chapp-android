package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.ErrorReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ErrorViewModel: ViewModel() {
    private val TAG = "ErrorViewModel"
    val NO_ERROR = -1
    private val _uiState = MutableStateFlow(ErrorReporter.ErrorState())
    val uiState: StateFlow<ErrorReporter.ErrorState> = _uiState.asStateFlow()
    var lastTempError = mutableIntStateOf(NO_ERROR)
        private set;

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
        Log.d(TAG, "Got error $e")
        _uiState.update { currentState ->
            currentState.copy(
                serverError = ErrorReporter.getErrorState().serverError,
                internetError = ErrorReporter.getErrorState().internetError,
                authError = ErrorReporter.getErrorState().authError
            )
        }
        // TODO what happens if we get an error while we are showing an error.
        ErrorReporter.getNextTempError()?.let {
            lastTempError.intValue = it.ordinal
        }
    }

    fun errorDismissed(e: ErrorReporter.ErrorTypes) {
        Log.d(TAG, "$e dismissed")
        ErrorReporter.getNextTempError()?.let {
            lastTempError.intValue = it.ordinal
        } ?: run {
            lastTempError.intValue = NO_ERROR
        }
    }
}