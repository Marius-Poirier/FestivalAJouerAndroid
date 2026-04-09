package com.example.frontend.ui.screens.workflow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.frontend.ui.utils.isNetworkAvailable
import androidx.lifecycle.viewModelScope
import com.example.frontend.FestivalApp
import com.example.frontend.core.auth.AuthManager
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.*
import com.example.frontend.data.repository.EditeurRepository
import com.example.frontend.data.repository.OfflineWorkflowRepository
import com.example.frontend.data.repository.WorkflowRepository
import com.example.frontend.ui.utils.isNetworkAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WorkflowTab(val label: String) {
    EDITEUR("Éditeurs"),
    JEUX("Jeux"),
    ZONE_TARIFAIRE("Zones tarifaires"),
    ZONE_DU_PLAN("Plan"),
    RESERVATIONS("Réservations")
}

data class WorkflowUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // true quand l'appareil n'a pas de connexion réseau
    val isOffline: Boolean = false,

    // Festival selector
    val festivals: List<FestivalDto> = emptyList(),
    val selectedFestivalId: Int? = null,

    // Active tab
    val activeTab: WorkflowTab = WorkflowTab.EDITEUR,

    // Onglet Éditeurs
    val editeurs: List<EditeurDto> = emptyList(),
    val editeursSearch: String = "",
    val selectedStatutFilter: StatutWorkflow? = null,
    val reservationByEditeur: Map<Int, ReservationDto> = emptyMap(),

    // Onglet Jeux
    val jeuxFestival: List<JeuFestivalViewDto> = emptyList(),
    val jeuxSearch: String = "",

    // Onglet Zone Tarifaire
    val zonesTarifaires: List<ZoneTarifaireDto> = emptyList(),
    val showZoneTarifaireDialog: Boolean = false,
    val editingZoneTarifaire: ZoneTarifaireDto? = null,

    // Onglet Zone du Plan
    val zonesDuPlan: List<ZoneDuPlanDto> = emptyList(),
    val showZonePlanDialog: Boolean = false,
    val editingZonePlan: ZoneDuPlanDto? = null,
    val expandedZonePlanIds: Set<Int> = emptySet(),
    val tablesByZone: Map<Int, List<TableJeuDto>> = emptyMap(),
    val expandedTableIds: Set<Int> = emptySet(),
    val jeuxByTable: Map<Int, List<JeuTableDto>> = emptyMap(),

    // Onglet Réservations
    val reservations: List<ReservationDto> = emptyList(),
    val selectedReservationStatut: StatutWorkflow? = null,
    val expandedReservationIds: Set<Int> = emptySet(),
    val reservationJeux: Map<Int, List<JeuFestivalViewDto>> = emptyMap(),
    val reservationTables: Map<Int, List<TableJeuDto>> = emptyMap(),
    val expandedResaTableIds: Set<Int> = emptySet(),
    val resaTableJeux: Map<Int, List<JeuTableDto>> = emptyMap(),

    // Dialogs réservation
    val showAddResaTableDialog: Boolean = false,
    val resaForTableDialog: ReservationDto? = null,
    val selectedZoneForResaTable: Int? = null,
    val tablesForResaZone: List<TableJeuDto> = emptyList(),
    val showAddResaJeuDialog: Boolean = false,
    val resaForJeuDialog: ReservationDto? = null,
    val editeurJeuxForDialog: List<JeuDto> = emptyList(),
    val showAssignJeuToResaTableDialog: Boolean = false,
    val resaTableForJeuDialog: TableJeuDto? = null,
    val resaIdForJeuTableDialog: Int? = null
)

// ── AndroidViewModel reçoit l'Application en paramètre ────────────────────────
// C'est la seule différence avec ViewModel() : on peut accéder à applicationContext.
class WorkflowViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = WorkflowRepository(RetrofitInstance.workflowApi)
    private val editeurRepo = EditeurRepository(RetrofitInstance.editeurApi)
    val authManager: AuthManager = RetrofitInstance.authManager

    // Le repository hors-ligne, récupéré depuis FestivalApp (initialisé en Step 7)
    private val offlineRepo: OfflineWorkflowRepository =
        (application as FestivalApp).offlineRepository

    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState = _uiState.asStateFlow()

    private fun update(block: WorkflowUiState.() -> WorkflowUiState) {
        _uiState.value = _uiState.value.block()
    }

    init {
        loadFestivals()
    }

    // ── Chargement des festivals ───────────────────────────────────────────────

    fun loadFestivals() {
        viewModelScope.launch {
            val offline = !isNetworkAvailable(getApplication())
            update { copy(isOffline = offline) }
            //mode hors ligne
            if (offline) {
                // Mode hors-ligne : on essaie de charger le dernier festival mis en cache
                val cachedFestival = offlineRepo.getCachedFestival()
                if (cachedFestival != null) {
                    update { copy(festivals = listOf(cachedFestival)) }
                    cachedFestival.id?.let { selectFestival(it) }
                } else {
                    update { copy(error = "Aucune donnée hors-ligne disponible. Connectez-vous une première fois.") }
                }
                return@launch
            }

            // Mode en ligne
            try {
                val festivals = repo.getFestivals()
                update { copy(festivals = festivals) }
                val current = _uiState.value
                if (festivals.isNotEmpty() && current.selectedFestivalId == null) {
                    festivals.first().id?.let { selectFestival(it) }
                }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // ── Sélection d'un festival ────────────────────────────────────────────────

    fun selectFestival(id: Int) {
        update {
            copy(
                selectedFestivalId = id,
                isLoading = true,
                error = null,
                tablesByZone = emptyMap(),
                jeuxByTable = emptyMap(),
                zonesTarifaires = emptyList(),
                zonesDuPlan = emptyList(),
                expandedZonePlanIds = emptySet(),
                expandedTableIds = emptySet(),
                expandedReservationIds = emptySet(),
                reservationJeux = emptyMap(),
                reservationTables = emptyMap(),
                expandedResaTableIds = emptySet(),
                resaTableJeux = emptyMap()
            )
        }

        if (_uiState.value.isOffline) {
            // ── hors-ligne : lire depuis Room ───────────────────────
            viewModelScope.launch {
                try {
                    val editeurs     = offlineRepo.getEditeurs(festivalId = id)
                    val reservations = offlineRepo.getReservations(festivalId = id)
                    val jeux         = offlineRepo.getJeuxFestival(festivalId = id)
                    val reservationByEditeur = reservations
                        .filter { it.id != null }
                        .associateBy { it.editeurId }
                    update {
                        copy(
                            isLoading = false,
                            editeurs = editeurs,
                            jeuxFestival = jeux,
                            reservations = reservations,
                            reservationByEditeur = reservationByEditeur
                        )
                    }
                } catch (e: Exception) {
                    update { copy(isLoading = false, error = e.message) }
                }
            }
            return
        }

        // ── Chemin en ligne 
        viewModelScope.launch {
            try {
                val editeurs     = editeurRepo.getAll()
                val reservations = repo.getReservations(id)
                val jeux         = repo.getJeuFestivalView(id)
                val reservationByEditeur = reservations
                    .filter { it.id != null }
                    .associateBy { it.editeurId }
                update {
                    copy(
                        isLoading = false,
                        editeurs = editeurs,
                        jeuxFestival = jeux,
                        reservations = reservations,
                        reservationByEditeur = reservationByEditeur
                    )
                }

                // On sauvegarde tout en arrière-plan
                val festival = _uiState.value.festivals.firstOrNull { it.id == id }
                if (festival != null) {
                    saveWorkflowToRoom(id, festival, editeurs, reservations, jeux)
                }

                // Rechargement si on est déjà sur un onglet qui en a besoin
                val tab = _uiState.value.activeTab
                if (tab == WorkflowTab.ZONE_TARIFAIRE) loadZonesTarifaires(id)
                if (tab == WorkflowTab.ZONE_DU_PLAN) {
                    loadZonesDuPlan(id)
                    loadZonesTarifaires(id)
                }
            } catch (e: Exception) {
                update { copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Récupère TOUTES les données du workflow du festival et les sauvegarde dans Room.
     * Appelé en arrière-plan après que les données principales ont été affichées.
     * On charge tout
     */
    private suspend fun saveWorkflowToRoom(
        festivalId: Int,
        festival: FestivalDto,
        editeurs: List<EditeurDto>,
        reservations: List<ReservationDto>,
        jeux: List<JeuFestivalViewDto>
    ) {
        try {
            // Données des onglets non encore chargés
            val zonesTarifaires = repo.getZonesTarifaires(festivalId)
            val zonesDuPlan     = repo.getZonesDuPlan(festivalId)

            // Pour chaque zone → ses tables → les jeux sur chaque table
            val tablesByZone = mutableMapOf<Int, List<TableJeuDto>>()
            val jeusByTable  = mutableMapOf<Int, List<JeuTableDto>>()

            zonesDuPlan.forEach { zone ->
                val zoneId = zone.id ?: return@forEach
                val tables = repo.getTables(zoneId)
                tablesByZone[zoneId] = tables

                tables.forEach { table ->
                    val tableId = table.id ?: return@forEach
                    jeusByTable[tableId] = repo.getJeuxByTable(tableId)
                }
            }

            // Pour chaque réservation → ses tables
            val reservationTables = mutableMapOf<Int, List<TableJeuDto>>()
            reservations.forEach { resa ->
                val resaId = resa.id ?: return@forEach
                reservationTables[resaId] = repo.getReservationTables(resaId)
            }

            // Sauvegarde dans Room
            offlineRepo.saveAll(
                festival          = festival,
                editeurs          = editeurs,
                reservations      = reservations,
                jeuxFestival      = jeux,
                zonesTarifaires   = zonesTarifaires,
                zonesDuPlan       = zonesDuPlan,
                tablesByZone      = tablesByZone,
                jeusByTable       = jeusByTable,
                reservationTables = reservationTables
            )
        } catch (e: Exception) {
            android.util.Log.w("OfflineCache", "Erreur lors de la sauvegarde hors-ligne : ${e.message}")
        }
    }

    fun onTabSelected(tab: WorkflowTab) {
        update { copy(activeTab = tab) }
        val festivalId = _uiState.value.selectedFestivalId ?: return
        when (tab) {
            WorkflowTab.ZONE_TARIFAIRE -> {
                if (_uiState.value.zonesTarifaires.isEmpty()) loadZonesTarifaires(festivalId)
            }
            WorkflowTab.ZONE_DU_PLAN -> {
                if (_uiState.value.zonesDuPlan.isEmpty()) loadZonesDuPlan(festivalId)
                if (_uiState.value.zonesTarifaires.isEmpty()) loadZonesTarifaires(festivalId)
            }
            else -> {}
        }
    }

    // ── Éditeur tab ────────────────────────────────────────────────────────────

    fun onEditeursSearchChange(v: String) = update { copy(editeursSearch = v) }
    fun onStatutFilterChange(s: StatutWorkflow?) = update { copy(selectedStatutFilter = s) }

    // ── Jeux tab ───────────────────────────────────────────────────────────────

    fun onJeuxSearchChange(v: String) = update { copy(jeuxSearch = v) }

    // ── Zone Tarifaire tab ─────────────────────────────────────────────────────

    private fun loadZonesTarifaires(festivalId: Int) {
        viewModelScope.launch {
            try {
                val zones = if (_uiState.value.isOffline) {
                    offlineRepo.getZonesTarifaires(festivalId)
                } else {
                    repo.getZonesTarifaires(festivalId)
                }
                update { copy(zonesTarifaires = zones) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun showCreateZoneTarifaire() = update { copy(showZoneTarifaireDialog = true, editingZoneTarifaire = null) }
    fun showEditZoneTarifaire(zone: ZoneTarifaireDto) = update { copy(showZoneTarifaireDialog = true, editingZoneTarifaire = zone) }
    fun dismissZoneTarifaireDialog() = update { copy(showZoneTarifaireDialog = false, editingZoneTarifaire = null) }

    fun saveZoneTarifaire(nom: String, nombreTablesTotal: Int, prixTable: Double, prixM2: Double) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        val editing = _uiState.value.editingZoneTarifaire
        viewModelScope.launch {
            try {
                val req = CreateZoneTarifaireRequest(festivalId, nom, nombreTablesTotal, prixTable, prixM2)
                if (editing?.id != null) repo.updateZoneTarifaire(editing.id, req)
                else repo.createZoneTarifaire(req)
                dismissZoneTarifaireDialog()
                loadZonesTarifaires(festivalId)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun deleteZoneTarifaire(id: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                repo.deleteZoneTarifaire(id)
                loadZonesTarifaires(festivalId)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // ── Zone du Plan tab ───────────────────────────────────────────────────────

    private fun loadZonesDuPlan(festivalId: Int) {
        viewModelScope.launch {
            try {
                val zones = if (_uiState.value.isOffline) {
                    offlineRepo.getZonesDuPlan(festivalId)
                } else {
                    repo.getZonesDuPlan(festivalId)
                }
                update { copy(zonesDuPlan = zones) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun showCreateZonePlan() = update { copy(showZonePlanDialog = true, editingZonePlan = null) }
    fun showEditZonePlan(zone: ZoneDuPlanDto) = update { copy(showZonePlanDialog = true, editingZonePlan = zone) }
    fun dismissZonePlanDialog() = update { copy(showZonePlanDialog = false, editingZonePlan = null) }

    fun saveZonePlan(nom: String, nombreTables: Int, zoneTarifaireId: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        val editing = _uiState.value.editingZonePlan
        viewModelScope.launch {
            try {
                val req = CreateZoneDuPlanRequest(festivalId, nom, nombreTables, zoneTarifaireId)
                if (editing?.id != null) {
                    repo.updateZoneDuPlan(editing.id, req)
                    val existing = repo.getTables(editing.id)
                    val delta = nombreTables - existing.size
                    if (delta > 0) {
                        repeat(delta) {
                            repo.createTable(CreateTableRequest(editing.id, zoneTarifaireId, 2))
                        }
                    }
                    update { copy(tablesByZone = tablesByZone - editing.id) }
                } else {
                    repo.createZoneDuPlan(req)
                    val zones = repo.getZonesDuPlan(festivalId)
                    val newZone = zones.firstOrNull { it.nom == nom.trim() && it.zoneTarifaireId == zoneTarifaireId }
                    if (newZone?.id != null) {
                        repeat(nombreTables) {
                            repo.createTable(CreateTableRequest(newZone.id, zoneTarifaireId, 2))
                        }
                    }
                    update { copy(zonesDuPlan = zones) }
                }
                dismissZonePlanDialog()
                loadZonesDuPlan(festivalId)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun deleteZonePlan(id: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                repo.deleteZoneDuPlan(id)
                loadZonesDuPlan(festivalId)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun toggleZonePlanExpand(zoneId: Int) {
        val current = _uiState.value.expandedZonePlanIds
        if (zoneId in current) {
            update { copy(expandedZonePlanIds = current - zoneId) }
        } else {
            update { copy(expandedZonePlanIds = current + zoneId) }
            if (!_uiState.value.tablesByZone.containsKey(zoneId)) loadTablesForZone(zoneId)
        }
    }

    private fun loadTablesForZone(zoneId: Int) {
        viewModelScope.launch {
            try {
                val tables = if (_uiState.value.isOffline) {
                    offlineRepo.getTablesByZone(zoneId)
                } else {
                    repo.getTables(zoneId)
                }
                update { copy(tablesByZone = tablesByZone + (zoneId to tables)) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun toggleTableExpand(tableId: Int) {
        val current = _uiState.value.expandedTableIds
        if (tableId in current) {
            update { copy(expandedTableIds = current - tableId) }
        } else {
            update { copy(expandedTableIds = current + tableId) }
            if (!_uiState.value.jeuxByTable.containsKey(tableId)) loadJeuxForTable(tableId)
        }
    }

    private fun loadJeuxForTable(tableId: Int) {
        viewModelScope.launch {
            try {
                val jeux = if (_uiState.value.isOffline) {
                    offlineRepo.getJeuxByTable(tableId)
                } else {
                    repo.getJeuxByTable(tableId)
                }
                update { copy(jeusByTable = jeusByTable + (tableId to jeux)) }

            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // ── Reservations tab ───────────────────────────────────────────────────────

    fun onReservationStatutFilterChange(s: StatutWorkflow?) = update { copy(selectedReservationStatut = s) }

    fun loadReservations() {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                val reservations = if (_uiState.value.isOffline) {
                    offlineRepo.getReservations(festivalId)
                } else {
                    repo.getReservations(festivalId)
                }
                val reservationByEditeur = reservations.filter { it.id != null }.associateBy { it.editeurId }
                update { copy(reservations = reservations, reservationByEditeur = reservationByEditeur) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun deleteReservation(id: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                repo.deleteReservation(id)
                val reservations = repo.getReservations(festivalId)
                val reservationByEditeur = reservations.filter { it.id != null }.associateBy { it.editeurId }
                update { copy(reservations = reservations, reservationByEditeur = reservationByEditeur) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun toggleReservationExpand(resaId: Int) {
        val current = _uiState.value.expandedReservationIds
        if (resaId in current) {
            update { copy(expandedReservationIds = current - resaId) }
        } else {
            update { copy(expandedReservationIds = current + resaId) }
            if (!_uiState.value.reservationJeux.containsKey(resaId)) loadReservationJeux(resaId)
            if (!_uiState.value.reservationTables.containsKey(resaId)) loadReservationTables(resaId)
        }
    }

    private fun loadReservationJeux(resaId: Int) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                val jeux = if (_uiState.value.isOffline) {
                    offlineRepo.getJeuxByReservation(resaId)
                } else {
                    repo.getJeuFestivalView(festivalId, resaId)
                }
                update { copy(reservationJeux = reservationJeux + (resaId to jeux)) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    private fun loadReservationTables(resaId: Int) {
        viewModelScope.launch {
            try {
                val tables = if (_uiState.value.isOffline) {
                    offlineRepo.getReservationTables(resaId)
                } else {
                    repo.getReservationTables(resaId)
                }
                update { copy(reservationTables = reservationTables + (resaId to tables)) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun toggleResaTableExpand(tableId: Int) {
        val current = _uiState.value.expandedResaTableIds
        if (tableId in current) {
            update { copy(expandedResaTableIds = current - tableId) }
        } else {
            update { copy(expandedResaTableIds = current + tableId) }
            if (!_uiState.value.resaTableJeux.containsKey(tableId)) loadResaTableJeux(tableId)
        }
    }

    private fun loadResaTableJeux(tableId: Int) {
        viewModelScope.launch {
            try {
                val jeux = if (_uiState.value.isOffline) {
                    offlineRepo.getJeuxByTable(tableId)
                } else {
                    repo.getJeuxByTable(tableId)
                }
                update { copy(resaTableJeux = resaTableJeux + (tableId to jeux)) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // Dialog: add table to reservation
    fun showAddResaTableDialog(resa: ReservationDto) {
        val festivalId = _uiState.value.selectedFestivalId ?: return
        update {
            copy(
                showAddResaTableDialog = true,
                resaForTableDialog = resa,
                selectedZoneForResaTable = null,
                tablesForResaZone = emptyList()
            )
        }
        if (_uiState.value.zonesDuPlan.isEmpty()) loadZonesDuPlan(festivalId)
    }

    fun dismissAddResaTableDialog() = update {
        copy(
            showAddResaTableDialog = false,
            resaForTableDialog = null,
            selectedZoneForResaTable = null,
            tablesForResaZone = emptyList()
        )
    }

    fun selectZoneForResaTable(zoneId: Int) {
        update { copy(selectedZoneForResaTable = zoneId, tablesForResaZone = emptyList()) }
        viewModelScope.launch {
            try {
                val tables = repo.getTables(zoneId)
                val resaId = _uiState.value.resaForTableDialog?.id
                val reservedIds = if (resaId != null) {
                    (_uiState.value.reservationTables[resaId] ?: emptyList()).mapNotNull { it.id }.toSet()
                } else emptySet()

                val free = tables.filter { 
                    it.statut == StatutTable.LIBRE && 
                    (it.nbJeuxActuels == null || it.nbJeuxActuels == 0) &&
                    it.id !in reservedIds 
                }
                update { copy(tablesForResaZone = free) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun addTableToReservation(tableId: Int) {
        val resaId = _uiState.value.resaForTableDialog?.id ?: return
        viewModelScope.launch {
            try {
                repo.addTableToReservation(ReservationTableRequest(resaId, tableId))
                dismissAddResaTableDialog()
                val tables = repo.getReservationTables(resaId)
                update { copy(reservationTables = reservationTables + (resaId to tables)) }
                // Le backend ne met pas statut=RESERVE : on patche en local après le reload
                patchTableStatut(tableId, 0)
                loadReservations()
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun removeTableFromReservation(resaId: Int, tableId: Int) {
        viewModelScope.launch {
            try {
                repo.removeTableFromReservation(ReservationTableRequest(resaId, tableId))
                val tables = repo.getReservationTables(resaId)
                update { copy(reservationTables = reservationTables + (resaId to tables)) }
                // Le backend supprime aussi les JeuFestivalTable → 0 jeux sur la table
                // isReserved sera false (table plus dans reservationTables) → statut LIBRE
                patchTableStatut(tableId, 0)
                loadReservations()
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // Dialog: add jeu to reservation
    fun showAddResaJeuDialog(resa: ReservationDto) {
        update { copy(showAddResaJeuDialog = true, resaForJeuDialog = resa, editeurJeuxForDialog = emptyList()) }
        viewModelScope.launch {
            try {
                val jeux = editeurRepo.getJeux(resa.editeurId)
                update { copy(editeurJeuxForDialog = jeux) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun dismissAddResaJeuDialog() = update {
        copy(showAddResaJeuDialog = false, resaForJeuDialog = null, editeurJeuxForDialog = emptyList())
    }

    fun addJeuToReservation(jeuId: Int) {
        val resa = _uiState.value.resaForJeuDialog ?: return
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                repo.addJeuFestival(AddJeuFestivalRequest(jeuId, resa.id!!, festivalId, false, false, false))
                dismissAddResaJeuDialog()
                update { copy(reservationJeux = reservationJeux - resa.id) }
                loadReservationJeux(resa.id)
                val jeux = repo.getJeuFestivalView(festivalId)
                update { copy(jeuxFestival = jeux) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    fun removeJeuFromReservation(jeuFestivalId: Int, resaId: Int) {
        viewModelScope.launch {
            try {
                repo.deleteJeuFestival(jeuFestivalId)
                update { copy(reservationJeux = reservationJeux - resaId) }
                loadReservationJeux(resaId)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    // Dialog: assign jeu to resa table
    fun showAssignJeuToResaTableDialog(table: TableJeuDto, resa: ReservationDto) {
        val resaId = resa.id ?: return
        update {
            copy(
                showAssignJeuToResaTableDialog = true,
                resaTableForJeuDialog = table,
                resaIdForJeuTableDialog = resaId,
                editeurJeuxForDialog = emptyList()
            )
        }
        viewModelScope.launch {
            try {
                val jeux = editeurRepo.getJeux(resa.editeurId)
                update { copy(editeurJeuxForDialog = jeux) }
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
        table.id?.let { tableId ->
            if (!_uiState.value.resaTableJeux.containsKey(tableId)) loadResaTableJeux(tableId)
        }
        if (!_uiState.value.reservationJeux.containsKey(resaId)) loadReservationJeux(resaId)
    }

    fun dismissAssignJeuToResaTableDialog() = update {
        copy(
            showAssignJeuToResaTableDialog = false,
            resaTableForJeuDialog = null,
            resaIdForJeuTableDialog = null,
            editeurJeuxForDialog = emptyList()
        )
    }

    fun assignJeuToResaTable(jeuId: Int, tableId: Int) {
        val resaId = _uiState.value.resaIdForJeuTableDialog ?: return
        val festivalId = _uiState.value.selectedFestivalId ?: return
        viewModelScope.launch {
            try {
                var jeuFestivalId = (_uiState.value.reservationJeux[resaId] ?: emptyList())
                    .firstOrNull { it.jeuId == jeuId }?.id
                if (jeuFestivalId == null) {
                    repo.addJeuFestival(AddJeuFestivalRequest(jeuId, resaId, festivalId, false, false, false))
                    val refreshed = repo.getJeuFestivalView(festivalId, resaId)
                    update { copy(reservationJeux = reservationJeux + (resaId to refreshed)) }
                    jeuFestivalId = refreshed.firstOrNull { it.jeuId == jeuId }?.id
                }
                if (jeuFestivalId == null) {
                    update { copy(error = "Impossible de retrouver le jeu dans la réservation") }
                    return@launch
                }
                repo.assignJeuToTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                dismissAssignJeuToResaTableDialog()
                val actualJeux = repo.getJeuxByTable(tableId)
                update { copy(resaTableJeux = resaTableJeux + (tableId to actualJeux)) }
                val tables = repo.getReservationTables(resaId)
                update { copy(reservationTables = reservationTables + (resaId to tables)) }
                patchTableStatut(tableId, actualJeux.size)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }

    private fun patchTableStatut(tableId: Int, jeuxCount: Int) {
        val isReserved = _uiState.value.reservationTables.values.any { tables ->
            tables.any { it.id == tableId }
        }
        update {
            val updater = { table: TableJeuDto ->
                if (table.id == tableId) {
                    val cap = table.capaciteJeux ?: 2
                    val newStatut = when {
                        isReserved && jeuxCount >= cap -> StatutTable.PLEIN
                        isReserved -> StatutTable.RESERVE
                        else -> StatutTable.LIBRE
                    }
                    table.copy(nbJeuxActuels = jeuxCount, statut = newStatut)
                } else table
            }
            copy(
                tablesByZone = tablesByZone.mapValues { (_, tables) -> tables.map(updater) },
                reservationTables = reservationTables.mapValues { (_, tables) -> tables.map(updater) }
            )
        }
    }

    fun removeJeuFromResaTable(jeuId: Int, tableId: Int) {
        val resaId = _uiState.value.reservationTables.entries
            .firstOrNull { (_, tables) -> tables.any { it.id == tableId } }?.key
        viewModelScope.launch {
            try {
                val jeuFestivalId = if (resaId != null) {
                    (_uiState.value.reservationJeux[resaId] ?: emptyList())
                        .firstOrNull { it.jeuId == jeuId }?.id
                } else null
                if (jeuFestivalId == null) {
                    update { copy(error = "Impossible de retrouver l'association jeu-festival") }
                    return@launch
                }
                repo.removeJeuFromTable(JeuFestivalTableRequest(jeuFestivalId, tableId))
                val actualJeux = repo.getJeuxByTable(tableId)
                update { copy(resaTableJeux = resaTableJeux + (tableId to actualJeux)) }
                if (resaId != null) {
                    val tables = repo.getReservationTables(resaId)
                    update { copy(reservationTables = reservationTables + (resaId to tables)) }
                }
                patchTableStatut(tableId, actualJeux.size)
            } catch (e: Exception) {
                update { copy(error = e.message) }
            }
        }
    }
}
