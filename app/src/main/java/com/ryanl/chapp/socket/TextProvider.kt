package com.ryanl.chapp.socket

import com.ryanl.chapp.socket.models.TextMessage
import com.ryanl.chapp.util.Subscription

object TextProvider: Subscription<String, TextMessage>("Text") {
    suspend fun onNewText(msg: TextMessage) {
        notifyAll(msg)
        notifyOne(msg.to, msg)
        notifyOne(msg.from, msg)
    }
}