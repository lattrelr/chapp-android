package com.ryanl.chapp.api

import com.ryanl.chapp.api.models.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersService {
    @GET("users")
    suspend fun getUsers(): List<User>
    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String,
    ): Response<User>
}
