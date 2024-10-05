package com.ryanl.chapp.socket.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("status")
data class StatusMessage (
    override val type: String,
    val who: String,
    val status: String,
) : Message()
