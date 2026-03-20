package com.example.frontend_mobile_etape1.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditeurDto(
    val id: Int? = null,
    val nom: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateEditeurRequest(
    val nom: String,
    @SerialName("logo_url") val logoUrl: String? = null
)

@Serializable
data class PersonneDto(
    val id: Int? = null,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val fonction: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreatePersonneRequest(
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val fonction: String? = null
)
