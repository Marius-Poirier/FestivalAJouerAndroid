package com.example.frontend_mobile_etape1.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JeuFestivalDto(
    val id: Int? = null,
    @SerialName("jeu_id") val jeuId: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("dans_liste_demandee") val dansListeDemandee: Boolean,
    @SerialName("dans_liste_obtenue") val dansListeObtenue: Boolean,
    @SerialName("jeux_recu") val jeuxRecu: Boolean,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/** Vue enrichie utilisée dans le workflow */
@Serializable
data class JeuFestivalViewDto(
    val id: Int,
    @SerialName("jeu_id") val jeuId: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("dans_liste_demandee") val dansListeDemandee: Boolean,
    @SerialName("dans_liste_obtenue") val dansListeObtenue: Boolean,
    @SerialName("jeux_recu") val jeuxRecu: Boolean,
    @SerialName("jeu_nom") val jeuNom: String? = null,
    @SerialName("type_jeu_nom") val typeJeuNom: String? = null,
    @SerialName("nb_joueurs_min") val nbJoueursMin: Int? = null,
    @SerialName("nb_joueurs_max") val nbJoueursMax: Int? = null,
    @SerialName("duree_minutes") val dureeMinutes: Int? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("url_image") val urlImage: String? = null,
    val prototype: Boolean? = null,
    @SerialName("editeur_id") val editeurId: Int,
    @SerialName("editeur_nom") val editeurNom: String? = null
)

@Serializable
data class JeuPlacementDto(
    @SerialName("jeu_id") val jeuId: Int,
    @SerialName("jeu_festival_id") val jeuFestivalId: Int,
    @SerialName("table_id") val tableId: Int,
    @SerialName("zone_du_plan_id") val zoneDuPlanId: Int,
    @SerialName("zone_du_plan_nom") val zoneDuPlanNom: String
)

@Serializable
data class AddJeuFestivalRequest(
    @SerialName("jeu_id") val jeuId: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("dans_liste_demandee") val dansListeDemandee: Boolean = true,
    @SerialName("dans_liste_obtenue") val dansListeObtenue: Boolean = false,
    @SerialName("jeux_recu") val jeuxRecu: Boolean = false
)
