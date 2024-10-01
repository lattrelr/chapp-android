package com.ryanl.chapp.api

import com.ryanl.chapp.models.Login
import com.ryanl.chapp.models.ResponseLogin
import com.ryanl.chapp.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SessionsService {
    /*@GET("sessions/active")
    suspend fun checkActive(): List<User>
    @GET("sessions/logout")
    suspend fun logout(): List<User>*/
    @POST("sessions/login")
    suspend fun login(@Body login: Login): ResponseLogin
}