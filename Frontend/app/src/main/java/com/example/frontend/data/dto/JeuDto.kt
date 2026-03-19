package com.example.frontend.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JeuDto(
    val id: Int? = null,
    val nom: String,
    @SerialName("nb_joueurs_min") val nbJoueursMin: Int? = null,
    @SerialName("nb_joueurs_max") val nbJoueursMax: Int? = null,
    @SerialName("duree_minutes") val dureeMinutes: Int? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("age_max") val ageMax: Int? = null,
    val description: String? = null,
    @SerialName("lien_regles") val lienRegles: String? = null,
    val theme: String? = null,
    @SerialName("url_image") val urlImage: String? = null,
    @SerialName("url_video") val urlVideo: String? = null,
    val prototype: Boolean? = null,
    @SerialName("type_jeu_id") val typeJeuId: Int? = null,
    @SerialName("type_jeu_nom") val typeJeuNom: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val editeurs: List<JeuEditeurRef>? = null,
    val mecanismes: List<JeuMecanismeRef>? = null
)

@Serializable
data class JeuEditeurRef(
    val id: Int,
    val nom: String
)

@Serializable
data class JeuMecanismeRef(
    val id: Int,
    val nom: String
)

@Serializable
data class CreateJeuRequest(
    val nom: String,
    @SerialName("nb_joueurs_min") val nbJoueursMin: Int? = null,
    @SerialName("nb_joueurs_max") val nbJoueursMax: Int? = null,
    @SerialName("duree_minutes") val dureeMinutes: Int? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("age_max") val ageMax: Int? = null,
    val description: String? = null,
    @SerialName("lien_regles") val lienRegles: String? = null,
    val theme: String? = null,
    @SerialName("url_image") val urlImage: String? = null,
    val prototype: Boolean? = null,
    @SerialName("type_jeu_id") val typeJeuId: Int? = null,
    @SerialName("editeurs_ids") val editeursIds: List<Int>? = null,
    @SerialName("mecanismes_ids") val mecanismesIds: List<Int>? = null
)

@Serializable
data class TypeJeuDto(
    val id: Int,
    val nom: String
)

@Serializable
data class MecanismeDto(
    val id: Int,
    val nom: String,
    val description: String? = null
)
