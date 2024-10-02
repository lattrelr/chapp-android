package com.ryanl.chapp.api

import android.util.Log
import com.ryanl.chapp.models.Login
import com.ryanl.chapp.models.Message
import com.ryanl.chapp.models.ResponseActive
import com.ryanl.chapp.models.ResponseLogin
import com.ryanl.chapp.models.User
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
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val usersService: UsersService by lazy {
        retrofit.create(UsersService::class.java)
    }

    private val sessionsService: SessionsService by lazy {
        retrofit.create(SessionsService::class.java)
    }

    suspend fun login(username: String, password: String): ResponseLogin {
        // TODO check if response isSuccessful ?
        Log.d(TAG, "Logging in...")
        return sessionsService.login(Login(username, password))
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
        return usersService.getUsers()
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