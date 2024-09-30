package com.ryanl.chapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String = "",
    val from: String = "",
    val to: String = ""
)
