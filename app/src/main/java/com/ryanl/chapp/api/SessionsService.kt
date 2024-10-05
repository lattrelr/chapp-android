package com.ryanl.chapp.api

import com.ryanl.chapp.api.models.Login
import com.ryanl.chapp.api.models.ResponseActive
import com.ryanl.chapp.api.models.ResponseLogin
import com.ryanl.chapp.api.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SessionsService {
    @GET("sessions/active")
    suspend fun active(@Header("Authorization") token: String): Response<ResponseActive>
    // TODO I don't really need this? I can just delete the token.
    /*@GET("sessions/logout")
    suspend fun logout(): List<User>*/
    @POST("sessions/login")
    suspend fun login(@Body login: Login): ResponseLogin
}