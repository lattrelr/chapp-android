package com.ryanl.chapp.socket

import com.ryanl.chapp.persist.HistorySyncJob
import com.ryanl.chapp.util.Subscription
import kotlinx.coroutines.launch

object ConnectionManager {
    // TODO start this after login screen moves to history screen.
    // TODO stop this on logout
    fun test(state: Boolean) {
        kotlinx.coroutines.MainScope().launch {
            //notifyAll(state)
            HistorySyncJob.onConnectionChanged(state)
            AuthenticationManager.onConnectionChanged(state)
            // TODO call..
            // HistorySyncJob
            // WebSocket (close on no conn only, on a conn available auth manager will start it)
            // AuthenticationManager
        }
    }

    fun start() {
        test(true)
    }

    fun stop() {

    }
}