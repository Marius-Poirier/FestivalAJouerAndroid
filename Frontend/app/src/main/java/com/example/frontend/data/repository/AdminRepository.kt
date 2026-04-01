package com.example.frontend.data.repository

import com.example.frontend.api.AdminApiService
import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.dto.UpdateRoleRequest
import com.example.frontend.data.dto.UpdateStatusRequest
import retrofit2.Response

class AdminRepository(private val api: AdminApiService) {

    suspend fun getAllUsers(): List<AdminUserDto> = api.getAllUsers()

    suspend fun updateUserStatus(userId: Int, statut: String): Response<Unit> =
        api.updateUserStatus(userId, UpdateStatusRequest(statut))

    suspend fun updateUserRole(userId: Int, role: String): Response<Unit> =
        api.updateUserRole(userId, UpdateRoleRequest(role))
}
