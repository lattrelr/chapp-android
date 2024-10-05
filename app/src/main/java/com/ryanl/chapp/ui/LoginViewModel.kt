package com.ryanl.chapp.ui

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.api.models.ResponseLogin
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel : ViewModel() {
    var loggedInState by mutableStateOf(false)
        private set
    private var wsJob: Job? = null

    fun doLogin(username: String, password: String) {
        Log.d(TAG, "doLogin: $username, $password - state is $loggedInState")
        // TODO clean this up, exception shouldn't be for all calls...
        viewModelScope.launch {
            try {
                val response = Api.login(username, password)
                Log.d(TAG, "Login response: $response")
                // Store token and userId for later
                StoredAppPrefs.setToken(response.token)
                StoredAppPrefs.setUserId(response.userId)
                // Trigger the UI to move to the next activity
                loggedInState = true
                // Start the socket now that we have a valid token
                wsJob?.cancelAndJoin()
                wsJob = viewModelScope.launch {
                    WebsocketClient.runForever(response.token)
                }
                //WebsocketClient.sendTextMessage("MMMMMM", response.userId)
            } catch (e: Exception) {
                // TODO we don't want to catch the cancellationException, be specific
                Log.e(TAG, "Get Users FAILED - ${e.message}")
            }
        }
    }

    // TODO this logic is ugly
    fun logout() {
        Log.e(TAG, "Logging out...")
        wsJob?.cancel()
        loggedInState = false
    }

    fun reset() {
        loggedInState = false
    }

    fun checkForActiveSession() {
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
                    loggedInState = true
                    wsJob?.cancelAndJoin()
                    wsJob = viewModelScope.launch {
                        WebsocketClient.runForever(StoredAppPrefs.getToken())
                    }
                    Log.d(TAG, "checkForActiveSession: DONE")
                    //WebsocketClient.sendTextMessage("MMMMMM", StoredAppPrefs.getUserId())
                }
            }
        }
    }
}