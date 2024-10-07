package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.api.models.ResponseLogin
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel() : ViewModel() {
    fun doLogin(username: String, password: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            var response: ResponseLogin? = null

            try {
                response = Api.login(username, password)
                Log.d(TAG, "Login response: $response")
            }  catch (e: Exception) {
                Log.e(TAG, "Get Users FAILED - ${e.message}")
            }

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

    fun logout() {
        Log.e(TAG, "Logging out...")
        StoredAppPrefs.setToken("")
        StoredAppPrefs.setUserId("")
        WebsocketClient.closeSocket()
    }

    fun checkForActiveSession(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            var session: ResponseActive? = null

            try {
                session = Api.checkForActiveSession(StoredAppPrefs.getToken())
            } catch (e: Exception) {
                Log.e(TAG, "Get session FAILED - ${e.message}")
            }

            session?.let {
                if (session.userId == StoredAppPrefs.getUserId()) {
                    Log.d(TAG, "checkForActiveSession: Session is valid for $session.userId")
                    WebsocketClient.runForever(StoredAppPrefs.getToken())
                    onDone(true)
                }
            }
        }
    }
}