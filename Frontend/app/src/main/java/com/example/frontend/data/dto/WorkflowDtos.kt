package com.example.frontend.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatutWorkflow(val label: String) {
    @SerialName("pas_contacte") PAS_CONTACTE("Pas contacté"),
    @SerialName("contact_pris") CONTACT_PRIS("Contact pris"),
    @SerialName("discussion_en_cours") DISCUSSION_EN_COURS("Discussion en cours"),
    @SerialName("sera_absent") SERA_ABSENT("Sera absent"),
    @SerialName("considere_absent") CONSIDERE_ABSENT("Considéré absent"),
    @SerialName("present") PRESENT("Présent"),
    @SerialName("facture") FACTURE("Facturé"),
    @SerialName("paiement_recu") PAIEMENT_RECU("Paiement reçu"),
    @SerialName("paiement_en_retard") PAIEMENT_EN_RETARD("Paiement en retard")
}

@Serializable
enum class StatutTable {
    @SerialName("libre") LIBRE,
    @SerialName("reserve") RESERVE,
    @SerialName("plein") PLEIN,
    @SerialName("hors_service") HORS_SERVICE
}

@Serializable
data class ZoneTarifaireDto(
    val id: Int? = null,
    @SerialName("festival_id") val festivalId: Int? = null,
    val nom: String,
    @SerialName("nombre_tables_total") val nombreTablesTotal: Int,
    @SerialName("prix_table") val prixTable: Double,
    @SerialName("prix_m2") val prixM2: Double,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
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
data class TableJeuDto(
    val id: Int? = null,
    @SerialName("zone_du_plan_id") val zoneDuPlanId: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("capacite_jeux") val capaciteJeux: Int,
    @SerialName("nb_jeux_actuels") val nbJeuxActuels: Int? = null,
    val statut: StatutTable? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class JeuTableDto(
    val id: Int,
    val nom: String? = null,
    @SerialName("url_image") val urlImage: String? = null,
    @SerialName("type_jeu_nom") val typeJeuNom: String? = null,
    val editeurs: String? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("age_max") val ageMax: Int? = null,
    val theme: String? = null
)

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
data class ReservationDto(
    val id: Int? = null,
    @SerialName("editeur_id") val editeurId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("statut_workflow") val statutWorkflow: StatutWorkflow? = null,
    @SerialName("editeur_presente_jeux") val editeurPresenteJeux: Boolean,
    @SerialName("remise_pourcentage") val remisePourcentage: Double? = null,
    @SerialName("remise_montant") val remiseMontant: Double? = null,
    @SerialName("prix_total") val prixTotal: Double? = null,
    @SerialName("prix_final") val prixFinal: Double? = null,
    @SerialName("commentaires_paiement") val commentairesPaiement: String? = null,
    @SerialName("paiement_relance") val paiementRelance: Boolean,
    @SerialName("date_facture") val dateFacture: String? = null,
    @SerialName("date_paiement") val datePaiement: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by") val updatedBy: Int? = null
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
data class CreateZoneDuPlanRequest(
    @SerialName("festival_id") val festivalId: Int,
    val nom: String,
    @SerialName("nombre_tables") val nombreTables: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int
)

@Serializable
data class CreateTableRequest(
    @SerialName("zone_du_plan_id") val zoneDuPlanId: Int,
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("capacite_jeux") val capaciteJeux: Int
)

@Serializable
data class JeuFestivalTableRequest(
    @SerialName("jeu_festival_id") val jeuFestivalId: Int,
    @SerialName("table_id") val tableId: Int
)

@Serializable
data class ReservationTableRequest(
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("table_id") val tableId: Int
)

@Serializable
data class AddJeuFestivalRequest(
    @SerialName("jeu_id") val jeuId: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("dans_liste_demandee") val dansListeDemandee: Boolean,
    @SerialName("dans_liste_obtenue") val dansListeObtenue: Boolean,
    @SerialName("jeux_recu") val jeuxRecu: Boolean
)

@Serializable
data class CreateReservationRequest(
    @SerialName("editeur_id") val editeurId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("statut_workflow") val statutWorkflow: StatutWorkflow? = null,
    @SerialName("editeur_presente_jeux") val editeurPresenteJeux: Boolean,
    @SerialName("paiement_relance") val paiementRelance: Boolean,
    @SerialName("remise_pourcentage") val remisePourcentage: Double? = null,
    @SerialName("commentaires_paiement") val commentairesPaiement: String? = null,
    @SerialName("date_facture") val dateFacture: String? = null,
    @SerialName("date_paiement") val datePaiement: String? = null
)
