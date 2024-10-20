package com.ryanl.chapp.persist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    // User id for primary key
    @PrimaryKey val id: String,
    var displayname: String,
    var username: String
)

// TODO add read/unread state to user if the chatview hasn't been opened since a message was
// TODO rxed