package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.UserMeResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("users/me")
    suspend fun getMe(): UserMeResponse

    @GET("users")
    suspend fun getUsers(@Query("search") search: String? = null): List<UserMeResponse>

    @GET("users/pending")
    suspend fun getPendingUsers(): List<UserMeResponse>

    @PATCH("users/{id}/validate")
    suspend fun validateUser(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>

    @PATCH("users/{id}/reject")
    suspend fun rejectUser(@Path("id") id: Int): Response<Unit>

    @PATCH("users/{id}/block")
    suspend fun blockUser(
        @Path("id") id: Int,
        @Body body: Map<String, Boolean>
    ): Response<Unit>
}
