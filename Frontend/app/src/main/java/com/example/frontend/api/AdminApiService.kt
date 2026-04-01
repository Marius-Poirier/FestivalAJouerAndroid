package com.example.frontend.api

import com.example.frontend.data.dto.AdminActionResponse
import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.dto.BlockUserRequest
import com.example.frontend.data.dto.ValidateUserRequest
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    @GET("users")
    suspend fun getAllUsers(): List<AdminUserDto>

    @PATCH("users/{id}/validate")
    suspend fun validateUser(
        @Path("id") id: Int,
        @Body body: ValidateUserRequest
    ): Response<AdminActionResponse>

    @PATCH("users/{id}/reject")
    suspend fun rejectUser(
        @Path("id") id: Int
    ): Response<AdminActionResponse>

    @PATCH("users/{id}/block")
    suspend fun blockUser(
        @Path("id") id: Int,
        @Body body: BlockUserRequest
    ): Response<AdminActionResponse>
}
