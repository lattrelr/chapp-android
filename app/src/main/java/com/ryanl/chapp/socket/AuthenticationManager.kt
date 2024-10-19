package com.ryanl.chapp.socket

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.persist.StoredAppPrefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AuthenticationManager {
    private const val TAG = "AuthenticationManager"
    private var authJob: Job? = null

    suspend fun onConnectionChanged(state: Boolean) {
        if (state) {
            if (tokenIsCached()) {
                authJob?.cancelAndJoin()
                authJob = kotlinx.coroutines.MainScope().launch {
                    while (true) {
                        if (!checkForActiveSession()) {
                            delay(5000)
                        }
                    }
                }
            }
        } else {
            authJob?.cancelAndJoin()
        }
    }

    // We'll force a logout when we get an unauthorized call
    // and a connection is detected, assume cached
    // credentials are valid on start so we can work
    // offline.
    fun tokenIsCached(): Boolean {
        return (
            StoredAppPrefs.getToken() != "" && StoredAppPrefs.getUserId() != ""
        )
    }

    private suspend fun checkForActiveSession(): Boolean {
        val session = Api.checkForActiveSession(StoredAppPrefs.getToken())

        if (session == null) {
            Log.e(TAG, "Failed to authenticate session")
            return false
        }

        if (session.userId == StoredAppPrefs.getUserId()) {
            Log.d(TAG, "checkForActiveSession: Session is valid for $session.userId")
            WebsocketClient.runForever(StoredAppPrefs.getToken())
        } else {
            logout()
            // TODO display to user somehow (overlay some message saying to click to reauth)
        }

        return true
    }

    fun logout() {
        Log.e(TAG, "Logging out...")
        StoredAppPrefs.setToken("")
        WebsocketClient.closeSocket()
        // TODO close historian ? Stop jobs...
        // TODO wipe database !?  Don't clear userId on logout, and if it changes on login wipe db!!
    }
}