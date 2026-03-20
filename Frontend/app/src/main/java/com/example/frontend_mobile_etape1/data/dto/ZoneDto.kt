package com.example.frontend_mobile_etape1.data.dto

import com.example.frontend_mobile_etape1.data.enums.StatutTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZoneTarifaireDto(
    val id: Int? = null,
    @SerialName("festival_id") val festivalId: Int?,
    val nom: String,
    @SerialName("nombre_tables_total") val nombreTablesTotal: Int,
    @SerialName("prix_table") val prixTable: Double,
    @SerialName("prix_m2") val prixM2: Double,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateZoneTarifaireRequest(
    @SerialName("festival_id") val festivalId: Int,
    val nom: String,
    @SerialName("nombre_tables_total") val nombreTablesTotal: Int,
    @SerialName("prix_table") val prixTable: Double,
    @SerialName("prix_m2") val prixM2: Double
)

@Serializable
data class ZoneDuPlanDto(
    val id: Int? = null,
    @SerialName("festival_id") val festivalId: Int,
    val nom: String,
    @SerialName("nombre_tables") val nombreTables: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateZoneDuPlanRequest(
    @SerialName("festival_id") val festivalId: Int,
    val nom: String,
    @SerialName("nombre_tables") val nombreTables: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int
)

/** Jeu retourné par GET /tables/:id/jeux */
@Serializable
data class JeuTableDto(
    val id: Int,
    val nom: String? = null,
    @SerialName("url_image") val urlImage: String?,
    @SerialName("type_jeu_nom") val typeJeuNom: String?,
    val editeurs: String?,
    @SerialName("age_min") val ageMin: Int?,
    @SerialName("age_max") val ageMax: Int?,
    val theme: String?
)

@Serializable
data class CreateTableRequest(
    @SerialName("zone_du_plan_id") val zoneDuPlanId: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("capacite_jeux") val capaciteJeux: Int = 2
)

@Serializable
data class JeuFestivalTableRequest(
    @SerialName("jeu_festival_id") val jeuFestivalId: Int,
    @SerialName("table_id") val tableId: Int
)

/** Lien entre une réservation et une table du plan (table reservation_tables) */
@Serializable
data class ReservationTableDto(
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("table_id") val tableId: Int,
    @SerialName("date_attribution") val dateAttribution: String? = null,
    @SerialName("attribue_par") val attribuePar: Int? = null
)

@Serializable
data class ReservationTableRequest(
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("table_id") val tableId: Int
)

@Serializable
data class TableJeuDto(
    val id: Int? = null,
    @SerialName("zone_du_plan_id") val zoneDuPlanId: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("capacite_jeux") val capaciteJeux: Int,
    @SerialName("nb_jeux_actuels") val nbJeuxActuels: Int?,
    val statut: StatutTable?,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
