package com.ryanl.chapp.api

import android.util.Log
import com.ryanl.chapp.api.models.Login
import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.api.models.ResponseLogin
import com.ryanl.chapp.api.models.User
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Api {
    private const val TAG = "Api"
    private const val BASE_URL = "http://10.0.2.2:30000/api/"
    //private const val BASE_URL = "http://webserver.local:3000/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            //.addConverterFactory(MoshiConverterFactory.create().asLenient())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val usersService: UsersService by lazy {
        retrofit.create(UsersService::class.java)
    }

    private val sessionsService: SessionsService by lazy {
        retrofit.create(SessionsService::class.java)
    }

    private val messagesService: MessagesService by lazy {
        retrofit.create(MessagesService::class.java)
    }

    suspend fun login(username: String, password: String): ResponseLogin? {
        Log.d(TAG, "Logging in...")
        val resp = sessionsService.login(Login(username, password))
        if (resp.isSuccessful) {
            return resp.body()
        }
        return null
    }

    suspend fun checkForActiveSession(token: String): ResponseActive? {
        Log.d(TAG, "Check session...")
        val resp = sessionsService.active("Bearer $token")
        if (resp.isSuccessful) {
            return resp.body()
        }
        return null
    }

    suspend fun getUsers(): List<User> {
        // TODO check if response isSuccessful ?
        Log.d(TAG, "Getting users...")
        val resp = usersService.getUsers()
        if (resp.isSuccessful) {
            return resp.body() ?: listOf()
        }
        return listOf()
    }

    suspend fun getUser(userId: String): User? {
        Log.d(TAG, "Getting user $userId...")
        val resp = usersService.getUser(userId)
        if (resp.isSuccessful) {
            return resp.body()
        }
        return null
    }

    suspend fun getConversation(user1: String, user2: String): List<Message> {
        Log.d(TAG, "Getting conversation...")
        val resp = messagesService.getConversation(user1, user2)
        if (resp.isSuccessful) {
            return resp.body() ?: listOf()
        }
        return listOf()
    }

    suspend fun getConversationAfter(user1: String, user2: String, timestamp: Long): List<Message> {
        Log.d(TAG, "Getting conversation for $user1 $user2 after $timestamp...")
        val resp = messagesService.getConversationAfter(user1, user2, timestamp)
        if (resp.isSuccessful) {
            return resp.body() ?: listOf()
        }
        return listOf()
    }

    /*fun getUsers(): List<User> {
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
    }*/

    /*fun getConversation(user1: String, user2: String): List<Message> {
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
    }*/

    fun getMyUserId(): String {
        return "0"
    }
}