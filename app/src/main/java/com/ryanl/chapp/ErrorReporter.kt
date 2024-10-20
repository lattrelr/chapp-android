package com.ryanl.chapp

import android.util.Log
import com.ryanl.chapp.util.Subscription

object ErrorReporter: Subscription<String, ErrorReporter.ErrorTypes>("Errors") {
    private const val TAG = "ErrorReporter"
    enum class ErrorTypes {SERV_ERR, NO_INTERNET, AUTH_ERR, SEND_FAILED}
    data class ErrorState(
        var serverError: Boolean = false,
        var internetError: Boolean = false,
        var authError: Boolean = false,
    )

    // TODO stack errors in a hierarchy, only clear or set one at a time
    // TODO temp vs persist errors? SEND_FAILED should only show briefly then go away.
    // TODO errorState for persistent errors, notify for temp?
    private val errorState = ErrorState()

    fun getErrorState(): ErrorState {
        return errorState
    }

    suspend fun setError(e: ErrorTypes) {
        Log.d(TAG, "Set error $e")
        when (e) {
            ErrorTypes.SERV_ERR -> { errorState.serverError = true }
            ErrorTypes.NO_INTERNET -> { errorState.internetError = true }
            ErrorTypes.AUTH_ERR -> { errorState.authError = true }
            ErrorTypes.SEND_FAILED -> {}
        }

        notifyAll(e)
    }

    suspend fun clearError(e: ErrorTypes) {
        when (e) {
            ErrorTypes.SERV_ERR -> { errorState.serverError = false }
            ErrorTypes.NO_INTERNET -> { errorState.internetError = false }
            ErrorTypes.AUTH_ERR -> { errorState.authError = false }
            ErrorTypes.SEND_FAILED -> {}
        }
    }
}