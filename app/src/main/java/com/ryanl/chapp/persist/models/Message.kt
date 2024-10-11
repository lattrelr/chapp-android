package com.ryanl.chapp.persist.models

import androidx.room.Entity

@Entity
data class Message(
    val msgId: String
)
