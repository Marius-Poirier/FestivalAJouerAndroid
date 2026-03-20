package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.AssignationApiService
import com.example.frontend_mobile_etape1.api.PlanApiService
import com.example.frontend_mobile_etape1.data.dto.*

class WorkflowRepository(
    private val planApi: PlanApiService,
    private val assignationApi: AssignationApiService
) {

    suspend fun getJeuxFestival(festivalId: Int? = null, reservationId: Int? = null): List<JeuFestivalViewDto> =
        assignationApi.getJeuxFestival(festivalId = festivalId, reservationId = reservationId)

    suspend fun addJeuFestival(request: AddJeuFestivalRequest) = assignationApi.addJeuFestival(request)

    suspend fun removeJeuFestival(id: Int) = assignationApi.removeJeuFestival(id)

    suspend fun getZonesTarifaires(festivalId: Int? = null): List<ZoneTarifaireDto> =
        planApi.getZonesTarifaires(festivalId = festivalId)

    suspend fun createZoneTarifaire(request: CreateZoneTarifaireRequest) =
        planApi.createZoneTarifaire(request)

    suspend fun updateZoneTarifaire(id: Int, request: CreateZoneTarifaireRequest) =
        planApi.updateZoneTarifaire(id, request)

    suspend fun deleteZoneTarifaire(id: Int) = planApi.deleteZoneTarifaire(id)

    suspend fun getZonesDuPlan(festivalId: Int? = null): List<ZoneDuPlanDto> =
        planApi.getZonesDuPlan(festivalId = festivalId)

    suspend fun createZoneDuPlan(request: CreateZoneDuPlanRequest) =
        planApi.createZoneDuPlan(request)

    suspend fun updateZoneDuPlan(id: Int, request: CreateZoneDuPlanRequest) =
        planApi.updateZoneDuPlan(id, request)

    suspend fun deleteZoneDuPlan(id: Int) = planApi.deleteZoneDuPlan(id)

    // ── Tables ────────────────────────────────────────────────────────────────

    suspend fun getTables(zoneDuPlanId: Int): List<TableJeuDto> =
        planApi.getTables(zoneDuPlanId = zoneDuPlanId)

    suspend fun getTable(id: Int): TableJeuDto = planApi.getTable(id)

    suspend fun getTableJeux(tableId: Int): List<JeuTableDto> =
        planApi.getTableJeux(tableId)

    suspend fun createTable(request: CreateTableRequest) = planApi.createTable(request)

    suspend fun deleteTable(id: Int) = planApi.deleteTable(id)

    // ── Assignation jeux ─────────────────────────────────────────────────────

    suspend fun assignJeuToTable(request: JeuFestivalTableRequest) =
        assignationApi.assignJeuToTable(request)

    suspend fun unassignJeuFromTable(request: JeuFestivalTableRequest) =
        assignationApi.unassignJeuFromTable(request)

    // ── Réservation ↔ Tables ──────────────────────────────────────────────────

    suspend fun getReservationTables(reservationId: Int): List<ReservationTableDto> =
        assignationApi.getReservationTables(reservationId = reservationId)

    suspend fun addTableToReservation(request: ReservationTableRequest) =
        assignationApi.addTableToReservation(request)

    suspend fun removeTableFromReservation(request: ReservationTableRequest) =
        assignationApi.removeTableFromReservation(request)
}
