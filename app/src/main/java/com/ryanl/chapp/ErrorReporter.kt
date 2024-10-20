package com.ryanl.chapp

import com.ryanl.chapp.util.Subscription

object ErrorReporter: Subscription<String, ErrorReporter.ErrorTypes>("Errors") {
    enum class ErrorTypes {NONE, SERV_ERR, NO_INTERNET, AUTH_ERR, SEND_FAILED}
    // TODO stack errors in a hierarchy, only clear or set one at a time
    // TODO temp vs persist errors? SEND_FAILED should only show briefly then go away.
    fun getErrors(): ErrorTypes {
        return ErrorTypes.NONE
    }

    suspend fun setError(e: ErrorTypes) {
        notifyAll(e)
    }

    suspend fun clearError(e: ErrorTypes) {

    }
}