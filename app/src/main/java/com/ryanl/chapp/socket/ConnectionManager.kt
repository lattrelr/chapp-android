package com.ryanl.chapp.socket

import com.ryanl.chapp.persist.HistorySyncJob
import com.ryanl.chapp.util.Subscription
import kotlinx.coroutines.launch

object ConnectionManager /*Subscription<String, Boolean>("Connection")*/ {
    fun test(state: Boolean) {
        kotlinx.coroutines.MainScope().launch {
            //notifyAll(state)
            HistorySyncJob.onConnectionChanged(state)
            // TODO call..
            // HistorySyncJob
            // WebSocket
            // AuthenticationManager
        }
    }
}