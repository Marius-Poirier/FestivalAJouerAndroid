package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.LoginRequest
import com.example.frontend_mobile_etape1.data.dto.LoginResponse
import com.example.frontend_mobile_etape1.data.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("auth/refresh")
    suspend fun refresh(): Response<Unit>
}