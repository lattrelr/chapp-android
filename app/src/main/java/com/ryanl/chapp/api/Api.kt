package com.ryanl.chapp.api

import com.ryanl.chapp.models.Message
import com.ryanl.chapp.models.User
import kotlinx.serialization.json.Json

object Api {
    fun getUsers(): List<User> {
        val jsonData = """
          [
            {"id": "0", "displayName": "ryan"},
            {"id": "1", "displayName": "Bob"},
            {"id": "2", "displayName": "Ken"},
            {"id": "3", "displayName": "Jan"},
            {"id": "4", "displayName": "Pam"}
          ]
        """.trimIndent()
        val myList = Json.decodeFromString<List<User>>(jsonData)
        return myList
    }

    fun getConversation(user1: String, user2: String): List<Message> {
        val jsonData = """
          [
            {"from": "0", "to": "1", "text": "Hey, how is it going?"},
            {"from": "1", "to": "0", "text": "I'm good, how are you?"},
            {"from": "0", "to": "1", "text": "Fine. Thanks!"},
            {"from": "1", "to": "0", "text": "What day is it tomorrow? Do you want to go swimming"},
            {"from": "0", "to": "1", "text": "No, I hate swimming"}
          ]
        """.trimIndent()
        val myList = Json.decodeFromString<List<Message>>(jsonData)
        return myList
    }

    fun getMyUserId(): String {
        return "0"
    }
}