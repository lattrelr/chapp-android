package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.api.models.ResponseLogin
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel() : ViewModel() {
    fun doLogin(username: String, password: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val response = Api.login(username, password)
            Log.d(TAG, "Login response: $response")

            response?.let {
                // Store token and userId for later
                StoredAppPrefs.setToken(response.token)
                StoredAppPrefs.setUserId(response.userId)
                // Start the socket now that we have a valid token
                WebsocketClient.runForever(response.token)
                // Trigger the UI to move to the next activity
                onDone(true)
            }
        }
    }

    // We'll force a logout in the AuthenticationManager
    // when a connection is detected, assume cached
    // credentials are valid on start so we can work
    // offline.
    fun tokenIsCached(onDone: (Boolean) -> Unit): Boolean {
        return (
            StoredAppPrefs.getToken() != "" &&
            StoredAppPrefs.getUserId() != ""
        )
    }

    fun logout() {
        Log.e(TAG, "Logging out...")
        StoredAppPrefs.setToken("")
        StoredAppPrefs.setUserId("")
        WebsocketClient.closeSocket()
        // TODO close historian!
        // TODO wipe database !?  Don't clear userId on logout, and if it changes on login wipe db!!
    }
}