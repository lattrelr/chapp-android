package com.ryanl.chapp.persist.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.socket.models.TextMessage

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val text: String,
    val from: String,
    val to: String,
    val date: Long
) {
    constructor(msg: Message): this(msg._id, msg.text, msg.from, msg.to, msg.date)
    constructor(msg: TextMessage): this(msg._id, msg.text, msg.from, msg.to, msg.date)
}
