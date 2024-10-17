package com.ryanl.chapp.socket

import android.util.Log
import com.ryanl.chapp.socket.models.TextMessage
import com.ryanl.chapp.util.Subscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ConnectionManager {
    val sub = Subscription<suspend (Boolean) -> Unit, Boolean>("Connection")

    fun test(state: Boolean) {
        kotlinx.coroutines.MainScope().launch {
            sub.notifyAll(state)
        }
    }
}