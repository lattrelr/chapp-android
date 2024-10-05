package com.ryanl.chapp.api

import com.ryanl.chapp.api.models.User
import retrofit2.http.GET

interface UsersService {
    @GET("users")
    suspend fun getUsers(): List<User>
}
