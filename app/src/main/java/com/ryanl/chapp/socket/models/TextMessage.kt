package com.ryanl.chapp.socket.models

import kotlinx.serialization.Serializable

@Serializable
data class TextMessage(
    val type: String,
    val from: String,
    val text: String,
    val to: String,
)
