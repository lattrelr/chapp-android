package com.ryanl.chapp.api.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class User(
    val displayname: String = "",
    val id: String = "",
    val online: Boolean = false
)
