package com.example.frontend.data.repository

import com.example.frontend.api.AdminApiService
import com.example.frontend.data.dto.AdminActionResponse
import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.dto.BlockUserRequest
import com.example.frontend.data.dto.ValidateUserRequest
import retrofit2.Response

class AdminRepository(private val api: AdminApiService) {

    suspend fun getAllUsers(): List<AdminUserDto> = api.getAllUsers()

    suspend fun validateUser(userId: Int, role: String): Response<AdminActionResponse> =
        api.validateUser(userId, ValidateUserRequest(role))

    suspend fun rejectUser(userId: Int): Response<AdminActionResponse> =
        api.rejectUser(userId)

    suspend fun blockUser(userId: Int, blocked: Boolean): Response<AdminActionResponse> =
        api.blockUser(userId, BlockUserRequest(blocked))
}
