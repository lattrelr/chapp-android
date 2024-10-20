package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.ErrorReporter
import kotlinx.coroutines.launch

class ErrorViewModel: ViewModel() {
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

    fun errorChanged(e: ErrorReporter.ErrorTypes) {

    }
}