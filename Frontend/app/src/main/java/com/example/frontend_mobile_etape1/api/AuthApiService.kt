package com.example.frontend_mobile_etape1.api

<<<<<<< HEAD:Frontend/app/src/main/java/com/example/frontend_mobile_etape1/api/AuthApiService.kt
import com.example.frontend_mobile_etape1.data.dto.LoginRequest
import com.example.frontend_mobile_etape1.data.dto.LoginResponse
import com.example.frontend_mobile_etape1.data.dto.RegisterRequest
=======
import com.example.frontend.data.dto.LoginRequest
import com.example.frontend.data.dto.LoginResponse
import com.example.frontend.data.dto.RegisterRequest
import com.example.frontend.data.dto.UserMeResponse
>>>>>>> 7665606 (-correction de la gestion de l'utilisateur connecté, ajout de la route auth/me et modification de whoiam pour stocker l'utilisateur courant):Frontend/app/src/main/java/com/example/frontend/api/auth/AuthApiService.kt
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    @GET("users/me")
    suspend fun getMe(): Response<UserMeResponse>
}