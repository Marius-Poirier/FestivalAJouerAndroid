package com.example.frontend_mobile_etape1.ui.screens.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.*
import com.example.frontend_mobile_etape1.data.enums.StatutTable
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import com.example.frontend_mobile_etape1.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WorkflowTab(val label: String) {
    EDITEUR("Éditeur"),
    JEUX("Jeux"),
    ZONE_TARIFAIRE("Zone Tarifaire"),
    ZONE_PLAN("Zone du Plan"),
    RESERVATIONS("Réservations")
}

data class WorkflowUiState(
    val isLoading: Boolean = false,
    val festivals: List<FestivalDto> = emptyList(),
    val selectedFestivalId: Int? = null,
    val activeTab: WorkflowTab = WorkflowTab.EDITEUR,

    // Éditeurs tab
    val editeurs: List<EditeurDto> = emptyList(),
    val editeursSearch: String = "",
    val selectedStatutFilter: StatutWorkflow? = null,

    // Jeux tab
    val jeuxFestival: List<JeuFestivalViewDto> = emptyList(),
    val jeuxSearch: String = "",

    // Zones tarifaires
    val zonesTarifaires: List<ZoneTarifaireDto> = emptyList(),
    val showZoneTarifaireDialog: Boolean = false,
    val editingZoneTarifaire: ZoneTarifaireDto? = null,

    // Zones du plan
    val zonesDuPlan: List<ZoneDuPlanDto> = emptyList(),
    val showZonePlanDialog: Boolean = false,
    val editingZonePlan: ZoneDuPlanDto? = null,

    // Tables (dans une zone du plan)
    val expandedZonePlanIds: Set<Int> = emptySet(),
    val tablesByZone: Map<Int, List<TableJeuDto>> = emptyMap(),
    val showAddTableDialog: Boolean = false,
    val addTableForZone: ZoneDuPlanDto? = null,
    val expandedTableIds: Set<Int> = emptySet(),
    val jeusByTable: Map<Int, List<JeuTableDto>> = emptyMap(),
    val showAssignJeuDialog: Boolean = false,
    val assignJeuForTable: TableJeuDto? = null,

    // Réservations
    val reservations: List<ReservationDto> = emptyList(),
    val reservationsSearch: String = "",
    val selectedReservationStatut: StatutWorkflow? = null,

    // Réservations - gestion étendue (expansion et chargement lazy)
    val expandedReservationIds: Set<Int> = emptySet(),
    val reservationJeux: Map<Int, List<JeuFestivalViewDto>> = emptyMap(),
    val reservationTables: Map<Int, List<TableJeuDto>> = emptyMap(),
    val expandedResaTableIds: Set<Int> = emptySet(),
    val resaTableJeux: Map<Int, List<JeuTableDto>> = emptyMap(),
    // Dialog : ajout d'une table à une réservation
    val showAddResaTableDialog: Boolean = false,
    val resaForTableDialog: ReservationDto? = null,
    val freeTablesByZone: Map<Int, List<TableJeuDto>> = emptyMap(),
    // Dialog : ajout d'un jeu à une réservation
    val showAddResaJeuDialog: Boolean = false,
    val resaForJeuDialog: ReservationDto? = null,
    val editeurJeuxForDialog: List<JeuDto> = emptyList(),
    // Dialog : assignation jeu à table dans une réservation
    val showAssignJeuToResaTableDialog: Boolean = false,
    val resaTableForJeuDialog: TableJeuDto? = null,
    val resaIdForJeuTableDialog: Int? = null,

    // Map editeurId → reservation
    val reservationByEditeur: Map<Int, ReservationDto> = emptyMap(),

    val error: String? = null
)

class WorkflowViewModel : ViewModel() {
    private val festivalRepository = FestivalRepository(RetrofitInstance.festivalApi)
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)
    private val workflowRepository = WorkflowRepository(RetrofitInstance.planApi, RetrofitInstance.assignationApi)
    private val reservationRepository = ReservationRepository(RetrofitInstance.reservationApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState = _uiState.asStateFlow()

    init { loadFestivals() }

    private fun loadFestivals() {
        viewModelScope.launch {
            try {
                val festivals = festivalRepository.getAll()
                val firstId = festivals.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    festivals = festivals,
                    selectedFestivalId = firstId
                )
                if (firstId != null) loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun onFestivalSelected(id: Int) {
        _uiState.value = _uiState.value.copy(selectedFestivalId = id)
        loadTabData()
    }

    fun onTabSelected(tab: WorkflowTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
        loadTabData()
    }

    fun loadTabData() {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                when (_uiState.value.activeTab) {
                    WorkflowTab.EDITEUR -> {
                        // Charger TOUS les éditeurs (sans filtre festivalId)
                        val editeurs = editeurRepository.getAll()
                        val reservations = reservationRepository.getAll(festivalId = festivalId)
                        val reservationByEditeur = reservations.associateBy { it.editeurId }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            editeurs = editeurs,
                            reservationByEditeur = reservationByEditeur
                        )
                    }
                    WorkflowTab.JEUX -> {
                        val jeux = workflowRepository.getJeuxFestival(festivalId = festivalId)
                        _uiState.value = _uiState.value.copy(isLoading = false, jeuxFestival = jeux)
                    }
                    WorkflowTab.ZONE_TARIFAIRE -> {
                        val zones = workflowRepository.getZonesTarifaires(festivalId = festivalId)
                        _uiState.value = _uiState.value.copy(isLoading = false, zonesTarifaires = zones)
                    }
                    WorkflowTab.ZONE_PLAN -> {
                        val zones = workflowRepository.getZonesDuPlan(festivalId = festivalId)
                        val zonesTarifaires = workflowRepository.getZonesTarifaires(festivalId = festivalId)
                        val jeux = workflowRepository.getJeuxFestival(festivalId = festivalId)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            zonesDuPlan = zones,
                            zonesTarifaires = zonesTarifaires,
                            jeuxFestival = jeux,
                            // Réinitialiser l'état d'expansion
                            expandedZonePlanIds = emptySet(),
                            tablesByZone = emptyMap(),
                            expandedTableIds = emptySet(),
                            jeusByTable = emptyMap()
                        )
                    }
                    WorkflowTab.RESERVATIONS -> {
                        val reservations = reservationRepository.getAll(festivalId = festivalId)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reservations = reservations,
                            expandedReservationIds = emptySet(),
                            reservationJeux = emptyMap(),
                            reservationTables = emptyMap(),
                            expandedResaTableIds = emptySet(),
                            resaTableJeux = emptyMap()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onEditeursSearch(query: String) { _uiState.value = _uiState.value.copy(editeursSearch = query) }
    fun onJeuxSearch(query: String) { _uiState.value = _uiState.value.copy(jeuxSearch = query) }
    fun onReservationsSearch(query: String) { _uiState.value = _uiState.value.copy(reservationsSearch = query) }
    fun onStatutFilter(statut: StatutWorkflow?) { _uiState.value = _uiState.value.copy(selectedStatutFilter = statut) }
    fun onReservationStatutFilter(statut: StatutWorkflow?) { _uiState.value = _uiState.value.copy(selectedReservationStatut = statut) }

    // ── Workflow Status Update ────────────────────────────────────────────────

    fun updateReservationStatus(editeurId: Int, newStatut: StatutWorkflow) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        val existingResa = _uiState.value.reservationByEditeur[editeurId]

        viewModelScope.launch {
            try {
                if (existingResa != null) {
                    val resaId = existingResa.id ?: return@launch
                    reservationRepository.update(
                        resaId,
                        UpdateReservationRequest(
                            statutWorkflow = newStatut.apiValue,
                            editeurPresenteJeux = null,
                            remisePourcentage = null,
                            remiseMontant = null,
                            commentairesPaiement = null,
                            paiementRelance = null,
                            dateFacture = null,
                            datePaiement = null
                        )
                    )
                } else {
                    // Créer une nouvelle réservation si elle n'existe pas
                    reservationRepository.create(
                        CreateReservationRequest(
                            editeurId = editeurId,
                            festivalId = festivalId,
                            statutWorkflow = newStatut.apiValue
                        )
                    )
                }
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la mise à jour du statut")
            }
        }
    }

    // ── Zone Tarifaire CRUD ────────────────────────────────────────────────────

    fun openZoneTarifaireDialog(zone: ZoneTarifaireDto? = null) {
        _uiState.value = _uiState.value.copy(showZoneTarifaireDialog = true, editingZoneTarifaire = zone)
    }

    fun dismissZoneTarifaireDialog() {
        _uiState.value = _uiState.value.copy(showZoneTarifaireDialog = false, editingZoneTarifaire = null)
    }

    fun saveZoneTarifaire(nom: String, nombreTables: Int, prixTable: Double, prixM2: Double) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        val editing = _uiState.value.editingZoneTarifaire
        viewModelScope.launch {
            try {
                val request = CreateZoneTarifaireRequest(
                    festivalId = festivalId, nom = nom,
                    nombreTablesTotal = nombreTables, prixTable = prixTable, prixM2 = prixM2
                )
                if (editing?.id != null) {
                    workflowRepository.updateZoneTarifaire(editing.id, request)
                } else {
                    workflowRepository.createZoneTarifaire(request)
                }
                dismissZoneTarifaireDialog()
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteZoneTarifaire(id: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.deleteZoneTarifaire(id)
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Zone du Plan CRUD ──────────────────────────────────────────────────────

    fun openZonePlanDialog(zone: ZoneDuPlanDto? = null) {
        _uiState.value = _uiState.value.copy(showZonePlanDialog = true, editingZonePlan = zone)
    }

    fun dismissZonePlanDialog() {
        _uiState.value = _uiState.value.copy(showZonePlanDialog = false, editingZonePlan = null)
    }

    fun saveZoneDuPlan(nom: String, zoneTarifaireId: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        val editing = _uiState.value.editingZonePlan
        viewModelScope.launch {
            try {
                val request = CreateZoneDuPlanRequest(
                    festivalId = festivalId, nom = nom,
                    nombreTables = editing?.nombreTables ?: 0,
                    zoneTarifaireId = zoneTarifaireId
                )
                if (editing?.id != null) {
                    workflowRepository.updateZoneDuPlan(editing.id, request)
                } else {
                    workflowRepository.createZoneDuPlan(request)
                }
                dismissZonePlanDialog()
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteZoneDuPlan(id: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.deleteZoneDuPlan(id)
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Tables ─────────────────────────────────────────────────────────────────

    fun toggleZonePlanExpand(zone: ZoneDuPlanDto) {
        val zoneId = zone.id ?: return
        val current = _uiState.value.expandedZonePlanIds
        if (zoneId in current) {
            _uiState.value = _uiState.value.copy(expandedZonePlanIds = current - zoneId)
        } else {
            _uiState.value = _uiState.value.copy(expandedZonePlanIds = current + zoneId)
            if (!_uiState.value.tablesByZone.containsKey(zoneId)) {
                loadTablesForZone(zoneId)
            }
        }
    }

    private fun loadTablesForZone(zoneDuPlanId: Int) {
        viewModelScope.launch {
            try {
                val tables = workflowRepository.getTables(zoneDuPlanId = zoneDuPlanId)
                val updated = _uiState.value.tablesByZone.toMutableMap()
                updated[zoneDuPlanId] = tables
                _uiState.value = _uiState.value.copy(tablesByZone = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun openAddTableDialog(zone: ZoneDuPlanDto) {
        _uiState.value = _uiState.value.copy(showAddTableDialog = true, addTableForZone = zone)
    }

    fun dismissAddTableDialog() {
        _uiState.value = _uiState.value.copy(showAddTableDialog = false, addTableForZone = null)
    }

    fun addTable(zone: ZoneDuPlanDto, capaciteJeux: Int) {
        val zoneId = zone.id ?: return
        viewModelScope.launch {
            try {
                workflowRepository.createTable(
                    CreateTableRequest(
                        zoneDuPlanId = zoneId,
                        zoneTarifaireId = zone.zoneTarifaireId,
                        capaciteJeux = capaciteJeux
                    )
                )
                dismissAddTableDialog()
                loadTablesForZone(zoneId)
                // Recharger les zones pour mettre à jour nombre_tables
                val festivalId = _uiState.value.selectedFestivalId ?: return@launch
                val zones = workflowRepository.getZonesDuPlan(festivalId = festivalId)
                _uiState.value = _uiState.value.copy(zonesDuPlan = zones)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteTable(tableId: Int, zoneDuPlanId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.deleteTable(tableId)
                loadTablesForZone(zoneDuPlanId)
                val festivalId = _uiState.value.selectedFestivalId ?: return@launch
                val zones = workflowRepository.getZonesDuPlan(festivalId = festivalId)
                _uiState.value = _uiState.value.copy(zonesDuPlan = zones)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Jeux dans les tables ───────────────────────────────────────────────────

    fun toggleTableExpand(table: TableJeuDto) {
        val tableId = table.id ?: return
        val current = _uiState.value.expandedTableIds
        if (tableId in current) {
            _uiState.value = _uiState.value.copy(expandedTableIds = current - tableId)
        } else {
            _uiState.value = _uiState.value.copy(expandedTableIds = current + tableId)
            if (!_uiState.value.jeusByTable.containsKey(tableId)) {
                loadJeuxForTable(tableId)
            }
        }
    }

    private fun loadJeuxForTable(tableId: Int) {
        viewModelScope.launch {
            try {
                val jeux = workflowRepository.getTableJeux(tableId)
                val updated = _uiState.value.jeusByTable.toMutableMap()
                updated[tableId] = jeux
                _uiState.value = _uiState.value.copy(jeusByTable = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun openAssignJeuDialog(table: TableJeuDto) {
        _uiState.value = _uiState.value.copy(showAssignJeuDialog = true, assignJeuForTable = table)
    }

    fun dismissAssignJeuDialog() {
        _uiState.value = _uiState.value.copy(showAssignJeuDialog = false, assignJeuForTable = null)
    }

    fun assignJeuToTable(jeuFestivalId: Int, tableId: Int) {
        val zoneDuPlanId = _uiState.value.assignJeuForTable?.zoneDuPlanId
        viewModelScope.launch {
            try {
                workflowRepository.assignJeuToTable(
                    JeuFestivalTableRequest(jeuFestivalId = jeuFestivalId, tableId = tableId)
                )
                dismissAssignJeuDialog()
                loadJeuxForTable(tableId)
                zoneDuPlanId?.let { loadTablesForZone(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unassignJeuFromTable(jeuId: Int, tableId: Int, zoneDuPlanId: Int) {
        // Retrouver jeu_festival_id depuis la liste des jeux du festival
        val jeuFestivalId = _uiState.value.jeuxFestival.firstOrNull { it.jeuId == jeuId }?.id
        if (jeuFestivalId == null) {
            _uiState.value = _uiState.value.copy(error = "Impossible de trouver l'association jeu-festival")
            return
        }
        viewModelScope.launch {
            try {
                workflowRepository.unassignJeuFromTable(
                    JeuFestivalTableRequest(jeuFestivalId = jeuFestivalId, tableId = tableId)
                )
                loadJeuxForTable(tableId)
                loadTablesForZone(zoneDuPlanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Réservations ───────────────────────────────────────────────────────────

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            try {
                reservationRepository.delete(id)
                _uiState.value = _uiState.value.copy(
                    expandedReservationIds = _uiState.value.expandedReservationIds - id,
                    reservationJeux = _uiState.value.reservationJeux - id,
                    reservationTables = _uiState.value.reservationTables - id
                )
                loadTabData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Réservations : gestion étendue ─────────────────────────────────────────

    fun toggleReservationExpand(resa: ReservationDto) {
        val resaId = resa.id ?: return
        val current = _uiState.value.expandedReservationIds
        if (resaId in current) {
            _uiState.value = _uiState.value.copy(expandedReservationIds = current - resaId)
        } else {
            _uiState.value = _uiState.value.copy(expandedReservationIds = current + resaId)
            if (!_uiState.value.reservationJeux.containsKey(resaId)) {
                loadReservationJeux(resaId, resa.festivalId)
            }
            if (!_uiState.value.reservationTables.containsKey(resaId)) {
                loadReservationTables(resaId)
            }
        }
    }

    private fun loadReservationJeux(resaId: Int, festivalId: Int) {
        viewModelScope.launch {
            try {
                val jeux = workflowRepository.getJeuxFestival(festivalId = festivalId, reservationId = resaId)
                val updated = _uiState.value.reservationJeux.toMutableMap()
                updated[resaId] = jeux
                _uiState.value = _uiState.value.copy(reservationJeux = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadReservationTables(resaId: Int) {
        viewModelScope.launch {
            try {
                val links = workflowRepository.getReservationTables(reservationId = resaId)
                val tables = links.mapNotNull { link ->
                    try { workflowRepository.getTable(link.tableId) } catch (_: Exception) { null }
                }
                val updated = _uiState.value.reservationTables.toMutableMap()
                updated[resaId] = tables
                var state = _uiState.value.copy(reservationTables = updated)
                // Charger les zones si nécessaire pour afficher les noms
                if (state.zonesDuPlan.isEmpty()) {
                    val festivalId = state.selectedFestivalId ?: run { _uiState.value = state; return@launch }
                    state = state.copy(zonesDuPlan = workflowRepository.getZonesDuPlan(festivalId = festivalId))
                }
                if (state.zonesTarifaires.isEmpty()) {
                    val festivalId = state.selectedFestivalId ?: run { _uiState.value = state; return@launch }
                    state = state.copy(zonesTarifaires = workflowRepository.getZonesTarifaires(festivalId = festivalId))
                }
                _uiState.value = state
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleResaTableExpand(tableId: Int) {
        val current = _uiState.value.expandedResaTableIds
        if (tableId in current) {
            _uiState.value = _uiState.value.copy(expandedResaTableIds = current - tableId)
        } else {
            _uiState.value = _uiState.value.copy(expandedResaTableIds = current + tableId)
            if (!_uiState.value.resaTableJeux.containsKey(tableId)) {
                loadResaTableJeux(tableId)
            }
        }
    }

    private fun loadResaTableJeux(tableId: Int) {
        viewModelScope.launch {
            try {
                val jeux = workflowRepository.getTableJeux(tableId)
                val updated = _uiState.value.resaTableJeux.toMutableMap()
                updated[tableId] = jeux
                _uiState.value = _uiState.value.copy(resaTableJeux = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Ajout/retrait table dans réservation ───────────────────────────────────

    fun openAddResaTableDialog(resa: ReservationDto) {
        _uiState.value = _uiState.value.copy(
            showAddResaTableDialog = true,
            resaForTableDialog = resa,
            freeTablesByZone = emptyMap()
        )
        if (_uiState.value.zonesDuPlan.isEmpty()) {
            val festivalId = _uiState.value.selectedFestivalId ?: return
            viewModelScope.launch {
                try {
                    val zones = workflowRepository.getZonesDuPlan(festivalId = festivalId)
                    _uiState.value = _uiState.value.copy(zonesDuPlan = zones)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }

    fun dismissAddResaTableDialog() {
        _uiState.value = _uiState.value.copy(
            showAddResaTableDialog = false,
            resaForTableDialog = null,
            freeTablesByZone = emptyMap()
        )
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

    fun addTableToReservation(resaId: Int, tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.addTableToReservation(ReservationTableRequest(resaId, tableId))
                dismissAddResaTableDialog()
                val updated = _uiState.value.reservationTables.toMutableMap()
                updated.remove(resaId)
                _uiState.value = _uiState.value.copy(reservationTables = updated)
                loadReservationTables(resaId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeTableFromReservation(resaId: Int, tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.removeTableFromReservation(ReservationTableRequest(resaId, tableId))
                val updatedTables = (_uiState.value.reservationTables[resaId] ?: emptyList())
                    .filter { it.id != tableId }
                val updated = _uiState.value.reservationTables.toMutableMap()
                updated[resaId] = updatedTables
                _uiState.value = _uiState.value.copy(
                    reservationTables = updated,
                    expandedResaTableIds = _uiState.value.expandedResaTableIds - tableId,
                    resaTableJeux = _uiState.value.resaTableJeux - tableId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Ajout/retrait jeu dans réservation ─────────────────────────────────────

    fun openAddResaJeuDialog(resa: ReservationDto) {
        _uiState.value = _uiState.value.copy(
            showAddResaJeuDialog = true,
            resaForJeuDialog = resa,
            editeurJeuxForDialog = emptyList()
        )
        viewModelScope.launch {
            try {
                val jeux = editeurRepository.getJeux(resa.editeurId)
                _uiState.value = _uiState.value.copy(editeurJeuxForDialog = jeux)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun dismissAddResaJeuDialog() {
        _uiState.value = _uiState.value.copy(
            showAddResaJeuDialog = false,
            resaForJeuDialog = null,
            editeurJeuxForDialog = emptyList()
        )
    }

    fun addJeuToReservation(resa: ReservationDto, jeuId: Int) {
        val resaId = resa.id ?: return
        viewModelScope.launch {
            try {
                workflowRepository.addJeuFestival(
                    AddJeuFestivalRequest(jeuId = jeuId, reservationId = resaId, festivalId = resa.festivalId)
                )
                dismissAddResaJeuDialog()
                loadReservationJeux(resaId, resa.festivalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeJeuFromReservation(jeuFestivalId: Int, resaId: Int, festivalId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.removeJeuFestival(jeuFestivalId)
                loadReservationJeux(resaId, festivalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Assignation jeu à table dans réservation ───────────────────────────────

    fun openAssignJeuToResaTableDialog(table: TableJeuDto, resaId: Int) {
        _uiState.value = _uiState.value.copy(
            showAssignJeuToResaTableDialog = true,
            resaTableForJeuDialog = table,
            resaIdForJeuTableDialog = resaId
        )
    }

    fun dismissAssignJeuToResaTableDialog() {
        _uiState.value = _uiState.value.copy(
            showAssignJeuToResaTableDialog = false,
            resaTableForJeuDialog = null,
            resaIdForJeuTableDialog = null
        )
    }

    fun assignJeuToResaTable(jeuFestivalId: Int, tableId: Int) {
        viewModelScope.launch {
            try {
                workflowRepository.assignJeuToTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                dismissAssignJeuToResaTableDialog()
                loadResaTableJeux(tableId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unassignJeuFromResaTable(jeuId: Int, tableId: Int, resaId: Int) {
        val jeuFestivalId = _uiState.value.reservationJeux[resaId]?.firstOrNull { it.jeuId == jeuId }?.id
        if (jeuFestivalId == null) {
            _uiState.value = _uiState.value.copy(error = "Impossible de trouver l'association jeu-festival")
            return
        }
        viewModelScope.launch {
            try {
                workflowRepository.unassignJeuFromTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                loadResaTableJeux(tableId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
