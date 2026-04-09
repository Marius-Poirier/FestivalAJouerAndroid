package com.example.frontend.data.database

import com.example.frontend.data.database.entity.*
import com.example.frontend.data.dto.*

// ═══════════════════════════════════════════════════════════════════════════
// FestivalDto ↔ FestivalEntity
// ═══════════════════════════════════════════════════════════════════════════
//convertiseur de dto en entity
fun FestivalDto.toEntity() = FestivalEntity(
    id        = this.id!!,
    nom       = this.nom,
    lieu      = this.lieu,
    dateDebut = this.dateDebut,
    dateFin   = this.dateFin
)

fun FestivalEntity.toDto() = FestivalDto(
    id        = this.id,
    nom       = this.nom,
    lieu      = this.lieu,
    dateDebut = this.dateDebut,
    dateFin   = this.dateFin
)

// ═══════════════════════════════════════════════════════════════════════════
// EditeurDto ↔ EditeurEntity
// ═══════════════════════════════════════════════════════════════════════════

fun EditeurDto.toEntity() = EditeurEntity(
    id      = this.id!!,
    nom     = this.nom,
    logoUrl = this.logoUrl
)

fun EditeurEntity.toDto() = EditeurDto(
    id      = this.id,
    nom     = this.nom,
    logoUrl = this.logoUrl
)

// ═══════════════════════════════════════════════════════════════════════════
// ReservationDto ↔ ReservationEntity
//
// StatutWorkflow est une enum. On stocke son nom brut (ex: "pas_contacte")
// et on le relit via enumValueOrNull() pour retrouver l'enum.
// ═══════════════════════════════════════════════════════════════════════════

fun ReservationDto.toEntity() = ReservationEntity(
    id                    = this.id!!,
    editeurId             = this.editeurId,
    festivalId            = this.festivalId,

    // on sauvegarde le nom sérialisé de l'enum (ex: "pas_contacte")
    statutWorkflow        = this.statutWorkflow?.name,

    editeurPresenteJeux   = this.editeurPresenteJeux,
    remisePourcentage     = this.remisePourcentage,
    remiseMontant         = this.remiseMontant,
    prixTotal             = this.prixTotal,
    prixFinal             = this.prixFinal,
    commentairesPaiement  = this.commentairesPaiement,
    paiementRelance       = this.paiementRelance,
    dateFacture           = this.dateFacture,
    datePaiement          = this.datePaiement
)

fun ReservationEntity.toDto() = ReservationDto(
    id                    = this.id,
    editeurId             = this.editeurId,
    festivalId            = this.festivalId,

    // on relit le String et on essaie de le convertir en enum (null si invalide)
    statutWorkflow        = this.statutWorkflow?.let { runCatching { StatutWorkflow.valueOf(it) }.getOrNull() },
    
    editeurPresenteJeux   = this.editeurPresenteJeux,
    remisePourcentage     = this.remisePourcentage,
    remiseMontant         = this.remiseMontant,
    prixTotal             = this.prixTotal,
    prixFinal             = this.prixFinal,
    commentairesPaiement  = this.commentairesPaiement,
    paiementRelance       = this.paiementRelance,
    dateFacture           = this.dateFacture,
    datePaiement          = this.datePaiement
)

// ═══════════════════════════════════════════════════════════════════════════
// JeuFestivalViewDto ↔ JeuFestivalEntity
// ═══════════════════════════════════════════════════════════════════════════

fun JeuFestivalViewDto.toEntity() = JeuFestivalEntity(
    id                = this.id,
    jeuId             = this.jeuId,
    reservationId     = this.reservationId,
    festivalId        = this.festivalId,
    dansListeDemandee = this.dansListeDemandee,
    dansListeObtenue  = this.dansListeObtenue,
    jeuxRecu          = this.jeuxRecu,
    jeuNom            = this.jeuNom,
    typeJeuNom        = this.typeJeuNom,
    nbJoueursMin      = this.nbJoueursMin,
    nbJoueursMax      = this.nbJoueursMax,
    dureeMinutes      = this.dureeMinutes,
    ageMin            = this.ageMin,
    urlImage          = this.urlImage,
    prototype         = this.prototype,
    editeurId         = this.editeurId,
    editeurNom        = this.editeurNom
)

fun JeuFestivalEntity.toDto() = JeuFestivalViewDto(
    id                = this.id,
    jeuId             = this.jeuId,
    reservationId     = this.reservationId,
    festivalId        = this.festivalId,
    dansListeDemandee = this.dansListeDemandee,
    dansListeObtenue  = this.dansListeObtenue,
    jeuxRecu          = this.jeuxRecu,
    jeuNom            = this.jeuNom,
    typeJeuNom        = this.typeJeuNom,
    nbJoueursMin      = this.nbJoueursMin,
    nbJoueursMax      = this.nbJoueursMax,
    dureeMinutes      = this.dureeMinutes,
    ageMin            = this.ageMin,
    urlImage          = this.urlImage,
    prototype         = this.prototype,
    editeurId         = this.editeurId,
    editeurNom        = this.editeurNom
)

// ═══════════════════════════════════════════════════════════════════════════
// ZoneTarifaireDto ↔ ZoneTarifaireEntity
// ═══════════════════════════════════════════════════════════════════════════

fun ZoneTarifaireDto.toEntity() = ZoneTarifaireEntity(
    id                = this.id!!,
    festivalId        = this.festivalId!!,
    nom               = this.nom,
    nombreTablesTotal = this.nombreTablesTotal,
    prixTable         = this.prixTable,
    prixM2            = this.prixM2
)

fun ZoneTarifaireEntity.toDto() = ZoneTarifaireDto(
    id                = this.id,
    festivalId        = this.festivalId,
    nom               = this.nom,
    nombreTablesTotal = this.nombreTablesTotal,
    prixTable         = this.prixTable,
    prixM2            = this.prixM2
)

// ═══════════════════════════════════════════════════════════════════════════
// ZoneDuPlanDto ↔ ZoneDuPlanEntity
// ═══════════════════════════════════════════════════════════════════════════

fun ZoneDuPlanDto.toEntity() = ZoneDuPlanEntity(
    id              = this.id!!,
    festivalId      = this.festivalId,
    nom             = this.nom,
    nombreTables    = this.nombreTables,
    zoneTarifaireId = this.zoneTarifaireId
)

fun ZoneDuPlanEntity.toDto() = ZoneDuPlanDto(
    id              = this.id,
    festivalId      = this.festivalId,
    nom             = this.nom,
    nombreTables    = this.nombreTables,
    zoneTarifaireId = this.zoneTarifaireId
)

// ═══════════════════════════════════════════════════════════════════════════
// TableJeuDto ↔ TableJeuEntity
// ═══════════════════════════════════════════════════════════════════════════

// Ici on a besoin du zoneId en paramètre car TableJeuDto ne le contient pas directement
fun TableJeuDto.toEntity(zoneDuPlanId: Int? = this.zoneDuPlanId) = TableJeuEntity(
    id              = this.id!!,
    zoneDuPlanId    = zoneDuPlanId,
    zoneTarifaireId = this.zoneTarifaireId,
    capaciteJeux    = this.capaciteJeux,
    nbJeuxActuels   = this.nbJeuxActuels,
    statut          = this.statut?.name
)

fun TableJeuEntity.toDto() = TableJeuDto(
    id              = this.id,
    zoneDuPlanId    = this.zoneDuPlanId,
    zoneTarifaireId = this.zoneTarifaireId,
    capaciteJeux    = this.capaciteJeux,
    nbJeuxActuels   = this.nbJeuxActuels,
    statut          = this.statut?.let { runCatching { StatutTable.valueOf(it) }.getOrNull() }
)

// ═══════════════════════════════════════════════════════════════════════════
// JeuTableDto ↔ JeuTableEntity
// ═══════════════════════════════════════════════════════════════════════════

fun JeuTableDto.toEntity(tableId: Int) = JeuTableEntity(
    id         = this.id,
    tableId    = tableId,
    nom        = this.nom,
    urlImage   = this.urlImage,
    typeJeuNom = this.typeJeuNom,
    editeurs   = this.editeurs,
    ageMin     = this.ageMin,
    ageMax     = this.ageMax,
    theme      = this.theme
)

fun JeuTableEntity.toDto() = JeuTableDto(
    id         = this.id,
    nom        = this.nom,
    urlImage   = this.urlImage,
    typeJeuNom = this.typeJeuNom,
    editeurs   = this.editeurs,
    ageMin     = this.ageMin,
    ageMax     = this.ageMax,
    theme      = this.theme
)
