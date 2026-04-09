package com.example.frontend.data.repository

import com.example.frontend.data.database.dao.*
import com.example.frontend.data.database.entity.ReservationTableEntity
import com.example.frontend.data.database.*
import com.example.frontend.data.dto.*

/**
 * Repository hors-ligne : lit et écrit les données du workflow dans Room (SQLite local).
 *
 * Deux rôles :
 *   1. saveAll(...) → appelé quand l'utilisateur est EN LIGNE et sélectionne un festival.
 *      Efface les anciennes données et sauvegarde le nouveau snapshot complet.
 *
 *   2. getXxx(...)  → appelé quand l'utilisateur est HORS LIGNE.
 *      Lit depuis Room et convertit les Entity en DTO pour que la UI reste inchangée.
 */
class OfflineWorkflowRepository(
    private val festivalDao: FestivalDao,
    private val editeurDao: EditeurDao,
    private val reservationDao: ReservationDao,
    private val jeuFestivalDao: JeuFestivalDao,
    private val zoneTarifaireDao: ZoneTarifaireDao,
    private val zoneDuPlanDao: ZoneDuPlanDao,
    private val tableJeuDao: TableJeuDao,
    private val jeuTableDao: JeuTableDao,
    private val reservationTableDao: ReservationTableDao
) {

    //ÉCRITURE (online → Room) 


     //Sauvegarde l'intégralité du workflow d'un festival dans Room.
    suspend fun saveAll(
        festival: FestivalDto,
        editeurs: List<EditeurDto>,
        reservations: List<ReservationDto>,
        jeuxFestival: List<JeuFestivalViewDto>,
        zonesTarifaires: List<ZoneTarifaireDto>,
        zonesDuPlan: List<ZoneDuPlanDto>,
        tablesByZone: Map<Int, List<TableJeuDto>>,
        jeuxByTable: Map<Int, List<JeuTableDto>>,
        reservationTables: Map<Int, List<TableJeuDto>>
    ) {
        // on repart de zéro à chaque synchronisation
        clearAll()

        festivalDao.upsert(festival.toEntity())

        editeurDao.insertAll(editeurs.map { it.toEntity() })
        
        reservationDao.insertAll(reservations.mapNotNull { r -> if (r.id != null) r.toEntity() else null})
        
        jeuFestivalDao.insertAll(jeuxFestival.map { it.toEntity() })
        
        zoneTarifaireDao.insertAll(zonesTarifaires.mapNotNull { z ->if (z.id != null && z.festivalId != null) z.toEntity() else null})
        
        zoneDuPlanDao.insertAll(zonesDuPlan.mapNotNull { z ->if (z.id != null) z.toEntity() else null})

        // ici on associe chque table avec sa zone du plan
        val allTables = tablesByZone.flatMap { (zoneId, tables) ->tables.mapNotNull { t -> if (t.id != null) t.toEntity(zoneId) else null }}
        tableJeuDao.insertAll(allTables)
        //on complete les informations des table avec la table jeutable 
        val allJeuxTable = jeuxByTable.flatMap { (tableId, jeux) ->jeux.map { j -> j.toEntity(tableId) }}
        jeuTableDao.insertAll(allJeuxTable)

        //on fait le lien avec la relation reservation table
        val allResaTables = reservationTables.flatMap { (resaId, tables) ->tables.mapNotNull { t -> if (t.id != null) ReservationTableEntity(resaId, t.id) else null }}
        reservationTableDao.insertAll(allResaTables)
    }

    suspend fun clearAll() {
        reservationTableDao.deleteAll()
        jeuTableDao.deleteAll()
        tableJeuDao.deleteAll()
        zoneDuPlanDao.deleteAll()
        zoneTarifaireDao.deleteAll()
        jeuFestivalDao.deleteAll()
        reservationDao.deleteAll()
        editeurDao.deleteAll()
        festivalDao.deleteAll()
    }

   //Retourne true si on a deja un festival stocker
    suspend fun hasCachedData(): Boolean = festivalDao.getFestival() != null


    // LECTURE (Room → ViewModel) 

    /* retourne le festival qui est dans la base de donnee*/
    suspend fun getCachedFestival(): FestivalDto? = festivalDao.getFestival()?.toDto()

    /** onglet editeur, il y a tout les editeur qui on fait une reservation*/
    suspend fun getEditeurs(festivalId: Int): List<EditeurDto> = editeurDao.getByFestivalFiltered(festivalId).map { it.toDto() }

    /** onglet reservation*/
    suspend fun getReservations(festivalId: Int): List<ReservationDto> = reservationDao.getByFestival(festivalId).map { it.toDto() }

    /**onglet Jeux*/
    suspend fun getJeuxFestival(festivalId: Int): List<JeuFestivalViewDto> = jeuFestivalDao.getByFestival(festivalId).map { it.toDto() }

    /** retourne les jeux d'une reservation pour le menu deroulant de reservation*/
    suspend fun getJeuxByReservation(reservationId: Int): List<JeuFestivalViewDto> = jeuFestivalDao.getByReservation(reservationId).map { it.toDto() }

    /** onglet zone tarifaire*/
    suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> = zoneTarifaireDao.getByFestival(festivalId).map { it.toDto() }

    /** onglet zone du plan*/
    suspend fun getZonesDuPlan(festivalId: Int): List<ZoneDuPlanDto> = zoneDuPlanDao.getByFestival(festivalId).map { it.toDto() }

    /** menu deroulant des tables dans zone du plan*/
    suspend fun getTablesByZone(zoneDuPlanId: Int): List<TableJeuDto> = tableJeuDao.getByZone(zoneDuPlanId).map { it.toDto() }

    /** menu deroulant des jeux placés sur une table*/
    suspend fun getJeuxByTable(tableId: Int): List<JeuTableDto> =
        jeuTableDao.getByTable(tableId).map { it.toDto() }

    //completer les colonne de table dans reservation avec tableJeu
    suspend fun getReservationTables(reservationId: Int): List<TableJeuDto> {
        val tableIds = reservationTableDao.getTableIdsByReservation(reservationId)
        return tableIds.mapNotNull { id -> tableJeuDao.getById(id)?.toDto() }
    }
}
