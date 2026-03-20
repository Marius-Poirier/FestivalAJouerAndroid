package com.example.frontend_mobile_etape1.data.dto

import com.example.frontend_mobile_etape1.data.enums.RoleUtilisateur
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

/** Réponse du POST /auth/login */
@Serializable
data class LoginResponse(
    val message: String?,
    val user: LoginUser?
)

@Serializable
data class LoginUser(
    val email: String,
    val role: String,
    @SerialName("statut_utilisateur") val statutUtilisateur: String?
)

/** Réponse du GET /users/me */
@Serializable
data class UserMeResponse(
    val id: Int,
    val email: String,
    val role: String,
    /** Le backend retourne "statut" (pas statut_utilisateur) pour /users/me */
    val statut: String?,
    @SerialName("date_demande") val dateDemande: String?,
    @SerialName("email_bloque") val emailBloque: Boolean?,
    @SerialName("created_at") val createdAt: String?
) {
    val roleEnum: RoleUtilisateur?
        get() = RoleUtilisateur.fromString(role)
}
