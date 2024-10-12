package com.ryanl.chapp.api.models

import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.socket.models.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val from: String,
    val to: String,
    val date: Long,
    val _id: String = "",
) {
    constructor(msg: Message): this(msg.text, msg.from, msg.to, msg.date, msg.id)
    constructor(msg: TextMessage): this(msg.text, msg.from, msg.to, msg.date, msg._id)
}
