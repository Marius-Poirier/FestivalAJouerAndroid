package com.example.frontend_mobile_etape1.data.dto

import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservationDto(
    val id: Int? = null,
    @SerialName("editeur_id") val editeurId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("statut_workflow") val statutWorkflow: StatutWorkflow?,
    @SerialName("editeur_presente_jeux") val editeurPresenteJeux: Boolean,
    @SerialName("remise_pourcentage") val remisePourcentage: Double?,
    @SerialName("remise_montant") val remiseMontant: Double?,
    @SerialName("prix_total") val prixTotal: Double?,
    @SerialName("prix_final") val prixFinal: Double?,
    @SerialName("commentaires_paiement") val commentairesPaiement: String?,
    @SerialName("paiement_relance") val paiementRelance: Boolean,
    @SerialName("date_facture") val dateFacture: String?,
    @SerialName("date_paiement") val datePaiement: String?,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by") val updatedBy: Int? = null
)

@Serializable
data class CreateReservationRequest(
    @SerialName("editeur_id") val editeurId: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("statut_workflow") val statutWorkflow: String = "pas_contacte",
    @SerialName("editeur_presente_jeux") val editeurPresenteJeux: Boolean = false,
    @SerialName("paiement_relance") val paiementRelance: Boolean = false
)

@Serializable
data class UpdateReservationRequest(
    @SerialName("statut_workflow") val statutWorkflow: String? = null,
    @SerialName("editeur_presente_jeux") val editeurPresenteJeux: Boolean? = null,
    @SerialName("remise_pourcentage") val remisePourcentage: Double? = null,
    @SerialName("remise_montant") val remiseMontant: Double? = null,
    @SerialName("commentaires_paiement") val commentairesPaiement: String? = null,
    @SerialName("paiement_relance") val paiementRelance: Boolean? = null,
    @SerialName("date_facture") val dateFacture: String? = null,
    @SerialName("date_paiement") val datePaiement: String? = null
)
