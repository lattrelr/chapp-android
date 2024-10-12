package com.ryanl.chapp.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val from: String,
    val to: String,
    val date: Long,
    val _id: String = "",
)
