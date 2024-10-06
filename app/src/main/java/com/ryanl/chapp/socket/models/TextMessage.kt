package com.ryanl.chapp.socket.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
data class TextMessage (
    override val type: String,
    val from: String,
    val text: String,
    val to: String,
    val _id: String,
) : Message()
