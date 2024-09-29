package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ryanl.chapp.socket.WebsocketClient

private const val TAG = "LoginViewModel"

class LoginViewModel : ViewModel() {
    var loggedInState by mutableStateOf(false)
        private set

    // TODO this should be in a coroutine
    fun doLogin(username: String, password: String) {
        // TODO websocket start shouldn't be here.
        WebsocketClient.start()
        Log.d(TAG, "doLogin: $username, $password - state is $loggedInState")
        loggedInState = true
    }

    fun reset() {
        loggedInState = false
    }

    fun check() {
        // TODO
        // check for valid token, set loggedInState if we are good
        // or delete token if we aren't
    }
}