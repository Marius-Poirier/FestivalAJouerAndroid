package com.example.frontend.data.repository

import com.example.frontend.api.WorkflowApiService
import com.example.frontend.data.dto.*

class WorkflowRepository(private val api: WorkflowApiService) {

    suspend fun getFestivals(): List<FestivalDto> = api.getFestivals()

    suspend fun getReservations(festivalId: Int): List<ReservationDto> =
        api.getReservations(festivalId)

    suspend fun createReservation(request: CreateReservationRequest) = api.createReservation(request)

    suspend fun updateReservation(id: Int, request: CreateReservationRequest) = api.updateReservation(id, request)

    suspend fun deleteReservation(id: Int) = api.deleteReservation(id)

    suspend fun getJeuFestivalView(festivalId: Int, reservationId: Int? = null): List<JeuFestivalViewDto> =
        api.getJeuFestivalView(festivalId, reservationId)

    suspend fun addJeuFestival(request: AddJeuFestivalRequest) = api.addJeuFestival(request)

    suspend fun deleteJeuFestival(id: Int) = api.deleteJeuFestival(id)

    suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> =
        api.getZonesTarifaires(festivalId)

    suspend fun createZoneTarifaire(request: CreateZoneTarifaireRequest) =
        api.createZoneTarifaire(request)

    suspend fun updateZoneTarifaire(id: Int, request: CreateZoneTarifaireRequest) =
        api.updateZoneTarifaire(id, request)

    suspend fun deleteZoneTarifaire(id: Int) = api.deleteZoneTarifaire(id)

    suspend fun getZonesDuPlan(festivalId: Int): List<ZoneDuPlanDto> =
        api.getZonesDuPlan(festivalId)

    suspend fun createZoneDuPlan(request: CreateZoneDuPlanRequest) = api.createZoneDuPlan(request)

    suspend fun updateZoneDuPlan(id: Int, request: CreateZoneDuPlanRequest) =
        api.updateZoneDuPlan(id, request)

    suspend fun deleteZoneDuPlan(id: Int) = api.deleteZoneDuPlan(id)

    suspend fun getTables(zoneDuPlanId: Int): List<TableJeuDto> = api.getTables(zoneDuPlanId)

    suspend fun getJeuxByTable(tableId: Int): List<JeuTableDto> = api.getJeuxByTable(tableId)

    suspend fun createTable(request: CreateTableRequest) = api.createTable(request)

    suspend fun deleteTable(id: Int) = api.deleteTable(id)

    suspend fun assignJeuToTable(request: JeuFestivalTableRequest) = api.assignJeuToTable(request)

    suspend fun removeJeuFromTable(request: JeuFestivalTableRequest) =
        api.removeJeuFromTable(request)

    suspend fun getReservationTables(reservationId: Int): List<TableJeuDto> =
        api.getReservationTables(reservationId)

    suspend fun addTableToReservation(request: ReservationTableRequest) =
        api.addTableToReservation(request)

    suspend fun removeTableFromReservation(request: ReservationTableRequest) =
        api.removeTableFromReservation(request)
}
