package com.ryanl.chapp.persist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey val id: String,
    val text: String,
    val from: String,
    val to: String,
    // TODO add timestamp
)
