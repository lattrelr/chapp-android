package com.ryanl.chapp.persist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    // User id for primary key
    @PrimaryKey val id: String,
    val displayname: String
)
