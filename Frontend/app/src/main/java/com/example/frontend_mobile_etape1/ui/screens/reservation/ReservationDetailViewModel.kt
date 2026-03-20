package com.example.frontend_mobile_etape1.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.*
import com.example.frontend_mobile_etape1.data.enums.StatutTable
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import com.example.frontend_mobile_etape1.data.repository.EditeurRepository
import com.example.frontend_mobile_etape1.data.repository.ReservationRepository
import com.example.frontend_mobile_etape1.data.repository.WorkflowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReservationDetailUiState(
    val isLoading: Boolean = false,
    val reservation: ReservationDto? = null,
    val editeur: EditeurDto? = null,
    val jeuxFestival: List<JeuFestivalViewDto> = emptyList(),
    val editeurJeux: List<JeuDto> = emptyList(),
    val tables: List<TableJeuDto> = emptyList(),
    val tableJeux: Map<Int, List<JeuTableDto>> = emptyMap(),
    val error: String? = null,
    val deleteSuccess: Boolean = false,

    // Dialogs
    val zonesDuPlan: List<ZoneDuPlanDto> = emptyList(),
    val freeTablesByZone: Map<Int, List<TableJeuDto>> = emptyMap(),
    val showAssignJeuToTableDialog: Boolean = false,
    val resaTableForJeuDialog: TableJeuDto? = null
)

class ReservationDetailViewModel(private val reservationId: Int) : ViewModel() {
    private val reservationRepository = ReservationRepository(RetrofitInstance.reservationApi)
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)
    private val workflowRepository = WorkflowRepository(RetrofitInstance.planApi, RetrofitInstance.assignationApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(ReservationDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resa = reservationRepository.getById(reservationId)
                val editeur = editeurRepository.getById(resa.editeurId)
                val jeux = workflowRepository.getJeuxFestival(festivalId = resa.festivalId, reservationId = reservationId)
                val editeurJeux = try { editeurRepository.getJeux(resa.editeurId) } catch (e: Exception) { emptyList() }
                val tables = loadTablesInternal()
                val zones = try { workflowRepository.getZonesDuPlan(resa.festivalId) } catch (e: Exception) { emptyList() }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reservation = resa,
                    editeur = editeur,
                    jeuxFestival = jeux,
                    editeurJeux = editeurJeux,
                    tables = tables,
                    zonesDuPlan = zones
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private suspend fun loadTablesInternal(): List<TableJeuDto> {
        return try {
            val links = workflowRepository.getReservationTables(reservationId)
            links.mapNotNull { link ->
                try { workflowRepository.getTable(link.tableId) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadTableJeux(tableId: Int) {
        viewModelScope.launch {
            try {
                val jeux = workflowRepository.getTableJeux(tableId)
                _uiState.value = _uiState.value.copy(
                    tableJeux = _uiState.value.tableJeux + (tableId to jeux)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    tableJeux = _uiState.value.tableJeux + (tableId to emptyList())
                )
            }
        }
    }

    fun addTable(tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.addTableToReservation(
                    ReservationTableRequest(reservationId = reservationId, tableId = tableId)
                )
                val updatedTables = loadTablesInternal()
                _uiState.value = _uiState.value.copy(tables = updatedTables)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de l'attribution de la table")
            }
        }
    }

    fun removeTable(tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.removeTableFromReservation(
                    ReservationTableRequest(reservationId = reservationId, tableId = tableId)
                )
                _uiState.value = _uiState.value.copy(
                    tables = _uiState.value.tables.filter { it.id != tableId },
                    tableJeux = _uiState.value.tableJeux - tableId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors du retrait de la table")
            }
        }
    }

    fun addJeu(jeuId: Int) {
        val resa = _uiState.value.reservation ?: return
        viewModelScope.launch {
            try {
                workflowRepository.addJeuFestival(
                    AddJeuFestivalRequest(
                        jeuId = jeuId,
                        reservationId = reservationId,
                        festivalId = resa.festivalId
                    )
                )
                val jeux = workflowRepository.getJeuxFestival(festivalId = resa.festivalId, reservationId = reservationId)
                _uiState.value = _uiState.value.copy(jeuxFestival = jeux)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de l'ajout du jeu")
            }
        }
    }

    fun removeJeu(linkId: Int) {
        val festivalId = _uiState.value.reservation?.festivalId ?: return
        viewModelScope.launch {
            try {
                workflowRepository.removeJeuFestival(linkId)
                val jeux = workflowRepository.getJeuxFestival(festivalId = festivalId, reservationId = reservationId)
                _uiState.value = _uiState.value.copy(jeuxFestival = jeux)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression du jeu")
            }
        }
    }

    fun loadFreeTablesForZone(zoneDuPlanId: Int) {
        viewModelScope.launch {
            try {
                val tables = workflowRepository.getTables(zoneDuPlanId).filter {
                    it.statut == StatutTable.LIBRE
                }
                val updated = _uiState.value.freeTablesByZone.toMutableMap()
                updated[zoneDuPlanId] = tables
                _uiState.value = _uiState.value.copy(freeTablesByZone = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun openAssignJeuToTable(table: TableJeuDto) {
        _uiState.value = _uiState.value.copy(
            showAssignJeuToTableDialog = true,
            resaTableForJeuDialog = table
        )
    }

    fun dismissAssignJeuToTableDialog() {
        _uiState.value = _uiState.value.copy(
            showAssignJeuToTableDialog = false,
            resaTableForJeuDialog = null
        )
    }

    fun assignJeuToTable(jeuFestivalId: Int, tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.assignJeuToTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                dismissAssignJeuToTableDialog()
                val updatedJeux = workflowRepository.getTableJeux(tableId)
                _uiState.value = _uiState.value.copy(
                    tableJeux = _uiState.value.tableJeux + (tableId to updatedJeux)
                )
                // Recharger les tables pour mettre à jour nbJeuxActuels
                val tables = loadTablesInternal()
                _uiState.value = _uiState.value.copy(tables = tables)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de l'assignation du jeu")
            }
        }
    }

    fun unassignJeuFromTable(jeuId: Int, tableId: Int) {
        val jeuFestivalId = _uiState.value.jeuxFestival.firstOrNull { it.jeuId == jeuId }?.id
        if (jeuFestivalId == null) {
            _uiState.value = _uiState.value.copy(error = "Impossible de trouver l'association jeu-festival")
            return
        }
        viewModelScope.launch {
            try {
                workflowRepository.unassignJeuFromTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                val updatedJeux = workflowRepository.getTableJeux(tableId)
                _uiState.value = _uiState.value.copy(
                    tableJeux = _uiState.value.tableJeux + (tableId to updatedJeux)
                )
                val tables = loadTablesInternal()
                _uiState.value = _uiState.value.copy(tables = tables)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la désassignation du jeu")
            }
        }
    }

    val canDelete: Boolean get() = authManager.isAdminSuperorgaOrga
    val canManageJeux: Boolean get() = authManager.isAdminSuperorgaOrga
    val canManageTables: Boolean get() = authManager.isAdminSuperorgaOrga

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                reservationRepository.delete(reservationId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
