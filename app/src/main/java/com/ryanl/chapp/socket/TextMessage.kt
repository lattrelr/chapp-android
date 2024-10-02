package com.ryanl.chapp.socket

import kotlinx.serialization.Serializable

@Serializable
data class TextMessage(
    val type: String = "text",
    val text: String,
    val to: String
)
