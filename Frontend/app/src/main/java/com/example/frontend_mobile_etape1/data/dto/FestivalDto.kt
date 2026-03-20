package com.example.frontend_mobile_etape1.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FestivalDto(
    val id: Int? = null,
    val nom: String,
    val lieu: String,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("date_fin") val dateFin: String,
    @SerialName("date_creation") val dateCreation: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateFestivalRequest(
    val nom: String,
    val lieu: String,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("date_fin") val dateFin: String
)
