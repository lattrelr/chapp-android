package com.ryanl.chapp.socket

import android.util.Log
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.persist.StoredAppPrefs
import kotlinx.coroutines.launch

object AuthenticationManager {
    private const val TAG = "AuthenticationManager"
    private lateinit var onInvalidAuth: () -> Unit

    fun start(cb: () -> Unit) {
        onInvalidAuth = cb
    }

    // TODO on a connection try until success or we cancel the job
    fun checkForActiveSession(onDone: (Boolean) -> Unit) {
        kotlinx.coroutines.MainScope().launch {
            val session = Api.checkForActiveSession(StoredAppPrefs.getToken())
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