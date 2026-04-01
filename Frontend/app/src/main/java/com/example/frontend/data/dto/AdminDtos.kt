package com.example.frontend.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Représentation d'un utilisateur renvoyé par GET /users (panel admin).
 */
@Serializable
data class AdminUserDto(
    val id: Int,
    val email: String,
    val role: String,
    val statut: String? = null,
    @SerialName("date_demande") val dateDemande: String? = null,
    @SerialName("email_bloque") val emailBloque: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Corps de la requête PATCH /users/{id}/status
 */
@Serializable
data class UpdateStatusRequest(
    val statut: String
)

/**
 * Corps de la requête PATCH /users/{id}/role
 */
@Serializable
data class UpdateRoleRequest(
    val role: String
)
