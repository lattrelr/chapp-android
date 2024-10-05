package com.ryanl.chapp.socket.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Message{
    abstract val type: String
}
