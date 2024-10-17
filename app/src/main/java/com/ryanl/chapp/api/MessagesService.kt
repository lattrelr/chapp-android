package com.ryanl.chapp.api

import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.api.models.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MessagesService {
    @GET("messages/{userId}")
    suspend fun getConversation(
        @Path("userId") user1: String,
        @Query("with") user2: String
    ): Response<List<Message>>
    @GET("messages/{userId}")
    suspend fun getConversationAfter(
        @Path("userId") user1: String,
        @Query("with") user2: String,
        @Query("after") timestamp: Long
    ): Response<List<Message>>
}
