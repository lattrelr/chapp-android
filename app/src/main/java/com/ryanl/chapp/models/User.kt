package com.ryanl.chapp.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class User(
    val displayName: String = "",
    val id: String = "",
    val online: Boolean = false
)
