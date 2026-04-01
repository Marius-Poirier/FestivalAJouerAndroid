package com.example.frontend.data.dto

import kotlinx.serialization.Serializable

/**
 * Représentation d'un utilisateur renvoyé par GET /users (panel admin).
 */
@Serializable
data class AdminUserDto(
    val id: Int,
    val email: String,
    val role: String,
    val statut: String,               // ex: "en_attente", "valide", "refuse"
    val date_demande: String? = null,
    val email_bloque: Boolean = false,
    val created_at: String? = null
)

/**
 * Corps de la requête PATCH /users/{id}/validate
 */
@Serializable
data class ValidateUserRequest(
    val role: String
)

/**
 * Corps de la requête PATCH /users/{id}/block
 */
@Serializable
data class BlockUserRequest(
    val blocked: Boolean
)

/**
 * Réponse des endpoints PATCH (validate, reject, block)
 */
@Serializable
data class AdminActionResponse(
    val message: String,
    val user: AdminUserDto
)

