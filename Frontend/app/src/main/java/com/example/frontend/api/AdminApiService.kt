package com.example.frontend.api

import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.dto.UpdateRoleRequest
import com.example.frontend.data.dto.UpdateStatusRequest
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    @GET("users")
    suspend fun getAllUsers(): List<AdminUserDto>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<Unit>

    @PATCH("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Int,
        @Body request: UpdateRoleRequest
    ): Response<Unit>
}
