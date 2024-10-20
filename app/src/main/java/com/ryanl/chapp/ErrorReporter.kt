package com.ryanl.chapp

import android.util.Log
import com.ryanl.chapp.util.Subscription
import java.util.LinkedList
import java.util.Queue

object ErrorReporter: Subscription<String, ErrorReporter.ErrorTypes>("Errors") {
    private const val TAG = "ErrorReporter"

    enum class ErrorTypes {
        SERV_ERR,
        NO_INTERNET,
        AUTH_ERR,
        SEND_FAILED;
        companion object {
            fun fromInt(value: Int) = ErrorTypes.entries[value]
        }
    }

    data class ErrorState(
        var serverError: Boolean = false,
        var internetError: Boolean = false,
        var authError: Boolean = false,
    )

    private val errorState = ErrorState()
    private val tempErrors: Queue<ErrorTypes> = LinkedList()

    fun getErrorState(): ErrorState {
        return errorState
    }

    fun getNextTempError(): ErrorTypes? {
        return tempErrors.poll()
    }

    suspend fun setError(e: ErrorTypes) {
        Log.d(TAG, "Set error $e")
        when (e) {
            ErrorTypes.SERV_ERR -> { errorState.serverError = true }
            ErrorTypes.NO_INTERNET -> { errorState.internetError = true }
            ErrorTypes.AUTH_ERR -> { errorState.authError = true }
            ErrorTypes.SEND_FAILED -> { tempErrors.add(e) }
        }

        notifyAll(e)
    }

    suspend fun clearError(e: ErrorTypes) {
        Log.d(TAG, "Clear error $e")
        when (e) {
            ErrorTypes.SERV_ERR -> { errorState.serverError = false }
            ErrorTypes.NO_INTERNET -> { errorState.internetError = false }
            ErrorTypes.AUTH_ERR -> { errorState.authError = false }
            ErrorTypes.SEND_FAILED -> {}
        }

        notifyAll(e)
    }
}