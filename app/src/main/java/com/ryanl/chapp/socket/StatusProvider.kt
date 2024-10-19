package com.ryanl.chapp.socket

import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.util.Subscription

object StatusProvider: Subscription<String, StatusMessage>("Status") {
    suspend fun onNewStatus(msg: StatusMessage) {
        notifyAll(msg)
        notifyOne(msg.who, msg)
    }
}