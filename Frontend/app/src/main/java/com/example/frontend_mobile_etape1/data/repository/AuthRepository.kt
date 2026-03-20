package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.AuthApiService
import com.example.frontend_mobile_etape1.data.dto.LoginRequest
import com.example.frontend_mobile_etape1.data.dto.LoginResponse
import com.example.frontend_mobile_etape1.data.dto.RegisterRequest
import retrofit2.Response

class AuthRepository(private val api: AuthApiService) {

    suspend fun login(email: String, password: String): Response<LoginResponse> =
        api.login(LoginRequest(email, password))

    suspend fun logout(): Response<Unit> = api.logout()

    suspend fun register(email: String, password: String): Response<Unit> =
        api.register(RegisterRequest(email, password))
}
