package com.ryanl.chapp.api

import com.ryanl.chapp.models.User
import kotlinx.serialization.json.Json

object Api {
    fun getUsers(): List<User> {
        val jsonData = """
          [
            {"id": "1", "displayName": "ryan"},
            {"id": "2", "displayName": "Bob"},
            {"id": "3", "displayName": "Ken"},
            {"id": "4", "displayName": "Jan"},
            {"id": "5", "displayName": "Pam"}
          ]
        """.trimIndent()
        val myList = Json.decodeFromString<List<User>>(jsonData)
        return myList
    }
}