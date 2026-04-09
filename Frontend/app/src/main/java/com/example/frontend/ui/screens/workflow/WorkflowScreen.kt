package com.example.frontend.ui.screens.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend.data.dto.*
import com.example.frontend.ui.components.*
import com.example.frontend.ui.theme.*

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun statutWorkflowColor(statut: StatutWorkflow?): Color = when (statut) {
    StatutWorkflow.PAS_CONTACTE -> Color(0xFF9E9E9E)
    StatutWorkflow.CONTACT_PRIS -> Color(0xFFF9A825)
    StatutWorkflow.DISCUSSION_EN_COURS -> Color(0xFF1565C0)
    StatutWorkflow.SERA_ABSENT -> Color(0xFFD32F2F)
    StatutWorkflow.CONSIDERE_ABSENT -> Color(0xFF7B1FA2)
    StatutWorkflow.PRESENT -> Color(0xFF2E7D32)
    StatutWorkflow.FACTURE -> Color(0xFF6A1B9A)
    StatutWorkflow.PAIEMENT_RECU -> Color(0xFF00695C)
    StatutWorkflow.PAIEMENT_EN_RETARD -> Color(0xFFE65100)
    null -> Color(0xFF9E9E9E)
}

private fun statutTableColor(statut: StatutTable?): Color = when (statut) {
    StatutTable.LIBRE -> Color(0xFF2E7D32)
    StatutTable.RESERVE -> Color(0xFFF9A825)
    StatutTable.PLEIN -> Color(0xFFD32F2F)
    StatutTable.HORS_SERVICE -> Color(0xFF9E9E9E)
    null -> Color(0xFF9E9E9E)
}

@Composable
private fun StatutWorkflowBadge(statut: StatutWorkflow?) {
    val color = statutWorkflowColor(statut)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            statut?.label ?: "Pas contacté",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun StatutTableBadge(statut: StatutTable?) {
    val color = statutTableColor(statut)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            statut?.name ?: "?",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun SectionLabel(text: String, count: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDBEAFE))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
            }
        }
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun WorkflowScreen(
    onEditReservation: (resaId: Int, festivalId: Int) -> Unit,
    onCreateReservation: (Int) -> Unit,
    viewModel: WorkflowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdminSuperorga = viewModel.authManager.isAdminSuperorga
    val isOrganisateurPlus = viewModel.authManager.isAdminSuperorgaOrga

    // En mode hors-ligne, lecture seul du workflow 
    val canWrite = !uiState.isOffline

    val visibleTabs = remember(isAdminSuperorga) {
        if (isAdminSuperorga) WorkflowTab.entries.toList()
        else listOf(WorkflowTab.EDITEUR, WorkflowTab.JEUX, WorkflowTab.RESERVATIONS)
    }
    val tabIndex = visibleTabs.indexOf(uiState.activeTab).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Workflow")

        // Festival selector
        FestivalSelector(
            festivals = uiState.festivals,
            selectedId = uiState.selectedFestivalId,
            onSelect = viewModel::selectFestival
        )

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.White,
            contentColor = BrightBlue,
            edgePadding = 8.dp
        ) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == tabIndex,
                    onClick = { viewModel.onTabSelected(tab) },
                    text = {
                        Text(
                            tab.label,
                            fontSize = 12.sp,
                            fontWeight = if (index == tabIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
            return@Column
        }

        // Bannière hors-ligne — toujours visible si pas de connexion
        if (uiState.isOffline) {
            OfflineBanner()
        }

        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(16.dp))
        }

        if (uiState.selectedFestivalId == null) {
            EmptyState("🎪", "Sélectionnez un festival")
            return@Column
        }

        when (uiState.activeTab) {
            WorkflowTab.EDITEUR -> EditeurTabContent(uiState, viewModel)
            WorkflowTab.JEUX -> JeuxTabContent(uiState, viewModel)

            WorkflowTab.ZONE_TARIFAIRE -> ZoneTarifaireTabContent(uiState, viewModel, isAdminSuperorga && canWrite)
            WorkflowTab.ZONE_DU_PLAN -> ZoneDuPlanTabContent(uiState, viewModel, isAdminSuperorga && canWrite)
            WorkflowTab.RESERVATIONS -> ReservationsTabContent(
                uiState, viewModel, isOrganisateurPlus && canWrite,
                onEditReservation = { resaId ->
                    onEditReservation(resaId, uiState.selectedFestivalId ?: 0)
                },
                onCreateReservation = { uiState.selectedFestivalId?.let(onCreateReservation) }
            )
        }
    }
}

// ── Offline Banner 
@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3CD))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            tint = Color(0xFF856404),
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = "Mode hors-ligne",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF856404)
            )
            Text(
                text = "Données du dernier festival consulté — consultation uniquement",
                fontSize = 11.sp,
                color = Color(0xFF856404)
            )
        }
    }
}

// ── Festival Selector ─────────────────────────────────────────────────────────


@Composable
private fun FestivalSelector(
    festivals: List<FestivalDto>,
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = festivals.firstOrNull { it.id == selectedId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue)
        ) {
            Text(
                selected?.nom ?: "Choisir un festival",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            festivals.forEach { festival ->
                DropdownMenuItem(
                    text = { Text(festival.nom, fontSize = 14.sp) },
                    onClick = {
                        festival.id?.let(onSelect)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── Tab 1: Éditeurs ───────────────────────────────────────────────────────────

@Composable
private fun EditeurTabContent(uiState: WorkflowUiState, viewModel: WorkflowViewModel) {
    val filteredEditeurs = remember(uiState.editeurs, uiState.editeursSearch, uiState.selectedStatutFilter) {
        uiState.editeurs.filter { editeur ->
            val matchSearch = uiState.editeursSearch.isBlank() ||
                editeur.nom.contains(uiState.editeursSearch, ignoreCase = true)
            val resa = editeur.id?.let { uiState.reservationByEditeur[it] }
            val matchStatut = uiState.selectedStatutFilter == null ||
                (uiState.selectedStatutFilter == StatutWorkflow.PAS_CONTACTE && resa == null) ||
                (resa != null && resa.statutWorkflow == uiState.selectedStatutFilter)
            matchSearch && matchStatut
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SearchBar(
                value = uiState.editeursSearch,
                onValueChange = viewModel::onEditeursSearchChange,
                placeholder = "Rechercher un éditeur..."
            )
            Spacer(Modifier.height(8.dp))
            // Filter chips
            val allStatuts = listOf(null) + StatutWorkflow.entries
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allStatuts) { statut ->
                    FilterChip(
                        selected = uiState.selectedStatutFilter == statut,
                        onClick = { viewModel.onStatutFilterChange(statut) },
                        label = { Text(statut?.label ?: "Tous", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredEditeurs.isEmpty()) {
            EmptyState("📚", "Aucun éditeur trouvé")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEditeurs, key = { it.id ?: it.nom }) { editeur ->
                    val resa = editeur.id?.let { uiState.reservationByEditeur[it] }
                    EditeurWorkflowCard(editeur = editeur, reservation = resa)
                }
            }
        }
    }
}

@Composable
private fun EditeurWorkflowCard(editeur: EditeurDto, reservation: ReservationDto?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (editeur.logoUrl != null) {
                AsyncImage(
                    model = editeur.logoUrl,
                    contentDescription = editeur.nom,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                EditeurAvatar(editeur.nom)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(editeur.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                if (reservation != null) {
                    Text(
                        "Réservation #${reservation.id}",
                        fontSize = 10.sp,
                        color = BrightBlue
                    )
                }
            }
            StatutWorkflowBadge(reservation?.statutWorkflow)
        }
    }
}

// ── Tab 2: Jeux ───────────────────────────────────────────────────────────────

@Composable
private fun JeuxTabContent(uiState: WorkflowUiState, viewModel: WorkflowViewModel) {
    val filteredJeux = remember(uiState.jeuxFestival, uiState.jeuxSearch) {
        if (uiState.jeuxSearch.isBlank()) uiState.jeuxFestival
        else uiState.jeuxFestival.filter { it.jeuNom?.contains(uiState.jeuxSearch, ignoreCase = true) == true }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            value = uiState.jeuxSearch,
            onValueChange = viewModel::onJeuxSearchChange,
            placeholder = "Rechercher un jeu...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (filteredJeux.isEmpty()) {
            EmptyState("🎲", "Aucun jeu trouvé")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredJeux, key = { it.id }) { jeu ->
                    JeuFestivalCard(jeu = jeu)
                }
            }
        }
    }
}

@Composable
private fun JeuFestivalCard(jeu: JeuFestivalViewDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            if (jeu.urlImage != null) {
                AsyncImage(
                    model = jeu.urlImage,
                    contentDescription = jeu.jeuNom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(Color(0xFFDDE3EA)),
                    contentAlignment = Alignment.Center
                ) { Text("🎲", fontSize = 32.sp) }
            }
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(jeu.jeuNom ?: "Jeu sans nom", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue, maxLines = 2)
                if (jeu.typeJeuNom != null) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BadgeBlueBg).padding(horizontal = 4.dp, vertical = 2.dp)
                    ) { Text(jeu.typeJeuNom, fontSize = 9.sp, color = BadgeBlueText) }
                }
                val players = if (jeu.nbJoueursMin != null && jeu.nbJoueursMax != null) "👥 ${jeu.nbJoueursMin}–${jeu.nbJoueursMax}" else null
                val duration = jeu.dureeMinutes?.let { "⏱ ~${it}min" }
                listOfNotNull(players, duration).forEach { info ->
                    Text(info, fontSize = 10.sp, color = TextMuted)
                }
                if (jeu.editeurNom != null) {
                    Text(jeu.editeurNom, fontSize = 10.sp, color = TextMuted, maxLines = 1)
                }
            }
        }
    }
}

// ── Tab 3: Zone Tarifaire ─────────────────────────────────────────────────────

@Composable
private fun ZoneTarifaireTabContent(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    isAdminSuperorga: Boolean
) {
    var zoneToDelete by remember { mutableStateOf<ZoneTarifaireDto?>(null) }

    if (zoneToDelete != null) {
        ConfirmDeleteDialog(
            itemName = zoneToDelete!!.nom,
            onConfirm = {
                zoneToDelete!!.id?.let { viewModel.deleteZoneTarifaire(it) }
                zoneToDelete = null
            },
            onDismiss = { zoneToDelete = null }
        )
    }

    if (uiState.showZoneTarifaireDialog) {
        ZoneTarifaireDialog(
            editing = uiState.editingZoneTarifaire,
            onDismiss = viewModel::dismissZoneTarifaireDialog,
            onSave = { nom, nbTables, prixTable, prixM2 ->
                viewModel.saveZoneTarifaire(nom, nbTables, prixTable, prixM2)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isAdminSuperorga) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FabAdd(onClick = viewModel::showCreateZoneTarifaire)
            }
        }

        if (uiState.zonesTarifaires.isEmpty()) {
            EmptyState("💰", "Aucune zone tarifaire")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.zonesTarifaires, key = { it.id ?: it.nom }) { zone ->
                    ZoneTarifaireCard(
                        zone = zone,
                        canManage = isAdminSuperorga,
                        onEdit = { viewModel.showEditZoneTarifaire(zone) },
                        onDelete = { zoneToDelete = zone }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneTarifaireCard(
    zone: ZoneTarifaireDto,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${zone.prixTable}€/table", fontSize = 11.sp, color = TextMuted)
                    Text("${zone.prixM2}€/m²", fontSize = 11.sp, color = TextMuted)
                    Text("${zone.nombreTablesTotal} tables", fontSize = 11.sp, color = TextMuted)
                }
            }
            if (canManage) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ZoneTarifaireDialog(
    editing: ZoneTarifaireDto?,
    onDismiss: () -> Unit,
    onSave: (String, Int, Double, Double) -> Unit
) {
    var nom by remember(editing) { mutableStateOf(editing?.nom ?: "") }
    var nbTables by remember(editing) { mutableStateOf(editing?.nombreTablesTotal?.toString() ?: "") }
    var prixTable by remember(editing) { mutableStateOf(editing?.prixTable?.toString() ?: "") }
    var prixM2 by remember(editing) { mutableStateOf(editing?.prixM2?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Modifier la zone" else "Nouvelle zone tarifaire") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it }, label = { Text("Nom *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
                OutlinedTextField(
                    value = nbTables, onValueChange = { nbTables = it }, label = { Text("Nombre de tables *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
                OutlinedTextField(
                    value = prixTable, onValueChange = { prixTable = it }, label = { Text("Prix/table (€) *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
                OutlinedTextField(
                    value = prixM2, onValueChange = { prixM2 = it }, label = { Text("Prix/m² (€) *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val nb = nbTables.toIntOrNull() ?: return@TextButton
                val pt = prixTable.toDoubleOrNull() ?: return@TextButton
                val pm = prixM2.toDoubleOrNull() ?: return@TextButton
                if (nom.isBlank()) return@TextButton
                onSave(nom.trim(), nb, pt, pm)
            }) { Text("Enregistrer", color = BrightBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextMuted) }
        }
    )
}

// ── Tab 4: Zone du Plan ───────────────────────────────────────────────────────

@Composable
private fun ZoneDuPlanTabContent(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    isAdminSuperorga: Boolean
) {
    var zoneToDelete by remember { mutableStateOf<ZoneDuPlanDto?>(null) }

    if (zoneToDelete != null) {
        ConfirmDeleteDialog(
            itemName = zoneToDelete!!.nom,
            onConfirm = {
                zoneToDelete!!.id?.let { viewModel.deleteZonePlan(it) }
                zoneToDelete = null
            },
            onDismiss = { zoneToDelete = null }
        )
    }

    if (uiState.showZonePlanDialog) {
        ZoneDuPlanDialog(
            editing = uiState.editingZonePlan,
            zonesTarifaires = uiState.zonesTarifaires,
            onDismiss = viewModel::dismissZonePlanDialog,
            onSave = { nom, nbTables, ztId -> viewModel.saveZonePlan(nom, nbTables, ztId) }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isAdminSuperorga) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FabAdd(onClick = viewModel::showCreateZonePlan)
            }
        }

        if (uiState.zonesDuPlan.isEmpty()) {
            EmptyState("🗺️", "Aucune zone du plan")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.zonesDuPlan, key = { it.id ?: it.nom }) { zone ->
                    val zoneId = zone.id ?: return@items
                    val isExpanded = zoneId in uiState.expandedZonePlanIds
                    ZoneDuPlanCard(
                        zone = zone,
                        isExpanded = isExpanded,
                        tables = uiState.tablesByZone[zoneId],
                        expandedTableIds = uiState.expandedTableIds,
                        jeusByTable = uiState.jeusByTable,
                        canManageZone = isAdminSuperorga,
                        onToggleExpand = { viewModel.toggleZonePlanExpand(zoneId) },
                        onEditZone = { viewModel.showEditZonePlan(zone) },
                        onDeleteZone = { zoneToDelete = zone },
                        onToggleTable = { tableId -> viewModel.toggleTableExpand(tableId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneDuPlanCard(
    zone: ZoneDuPlanDto,
    isExpanded: Boolean,
    tables: List<TableJeuDto>?,
    expandedTableIds: Set<Int>,
    jeusByTable: Map<Int, List<JeuTableDto>>,
    canManageZone: Boolean,
    onToggleExpand: () -> Unit,
    onEditZone: () -> Unit,
    onDeleteZone: () -> Unit,
    onToggleTable: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Zone header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextMuted, modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                    Text("${zone.nombreTables} table(s)", fontSize = 11.sp, color = TextMuted)
                }
                if (canManageZone) {
                    IconButton(onClick = onEditZone, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDeleteZone, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(15.dp))
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = BorderColor)
                Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp)) {
                    if (tables == null) {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    } else if (tables.isEmpty()) {
                        Text("Aucune table", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(8.dp))
                    } else {
                        tables.forEach { table ->
                            val tableId = table.id ?: return@forEach
                            val tableExpanded = tableId in expandedTableIds
                            TableRow(
                                table = table,
                                isExpanded = tableExpanded,
                                jeux = jeusByTable[tableId],
                                onToggle = { onToggleTable(tableId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRow(
    table: TableJeuDto,
    isExpanded: Boolean,
    jeux: List<JeuTableDto>?,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, tint = TextMuted, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Table #${table.id}",
                fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = NavyBlue,
                modifier = Modifier.width(80.dp)
            )
            Text(
                "${table.nbJeuxActuels ?: 0}/${table.capaciteJeux ?: "?"}",
                fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(50.dp)
            )
            StatutTableBadge(table.statut)
        }

        if (isExpanded) {
            if (jeux == null) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            } else {
                jeux.forEach { jeu ->
                    Text(
                        jeu.nom ?: "Jeu #${jeu.id}",
                        fontSize = 11.sp, color = NavyBlue,
                        modifier = Modifier.padding(start = 24.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ZoneDuPlanDialog(
    editing: ZoneDuPlanDto?,
    zonesTarifaires: List<ZoneTarifaireDto>,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int) -> Unit
) {
    var nom by remember(editing) { mutableStateOf(editing?.nom ?: "") }
    var nbTables by remember(editing) { mutableStateOf(editing?.nombreTables?.toString() ?: "") }
    var selectedZtId by remember(editing) { mutableStateOf(editing?.zoneTarifaireId ?: zonesTarifaires.firstOrNull()?.id) }
    var ztExpanded by remember { mutableStateOf(false) }
    val selectedZt = zonesTarifaires.firstOrNull { it.id == selectedZtId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Modifier la zone" else "Nouvelle zone du plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it }, label = { Text("Nom *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
                OutlinedTextField(
                    value = nbTables, onValueChange = { nbTables = it }, label = { Text("Nombre de tables *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                )
                Box {
                    OutlinedTextField(
                        value = selectedZt?.nom ?: "Choisir une zone tarifaire",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Zone tarifaire *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { ztExpanded = true }) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                    )
                    DropdownMenu(expanded = ztExpanded, onDismissRequest = { ztExpanded = false }) {
                        zonesTarifaires.forEach { zt ->
                            DropdownMenuItem(
                                text = { Text(zt.nom, fontSize = 13.sp) },
                                onClick = { selectedZtId = zt.id; ztExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val nb = nbTables.toIntOrNull() ?: return@TextButton
                val ztId = selectedZtId ?: return@TextButton
                if (nom.isBlank()) return@TextButton
                onSave(nom.trim(), nb, ztId)
            }) { Text("Enregistrer", color = BrightBlue) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = TextMuted) } }
    )
}

// ── Tab 5: Réservations ───────────────────────────────────────────────────────

@Composable
private fun ReservationsTabContent(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    isOrganisateurPlus: Boolean,
    onEditReservation: (Int) -> Unit,
    onCreateReservation: () -> Unit
) {
    var resaToDelete by remember { mutableStateOf<ReservationDto?>(null) }

    if (resaToDelete != null) {
        ConfirmDeleteDialog(
            itemName = "Réservation #${resaToDelete!!.id}",
            onConfirm = {
                resaToDelete!!.id?.let { viewModel.deleteReservation(it) }
                resaToDelete = null
            },
            onDismiss = { resaToDelete = null }
        )
    }

    if (uiState.showAddResaTableDialog && uiState.resaForTableDialog != null) {
        AddResaTableDialog(
            zones = uiState.zonesDuPlan,
            selectedZoneId = uiState.selectedZoneForResaTable,
            tables = uiState.tablesForResaZone,
            onSelectZone = viewModel::selectZoneForResaTable,
            onAddTable = viewModel::addTableToReservation,
            onDismiss = viewModel::dismissAddResaTableDialog
        )
    }

    if (uiState.showAddResaJeuDialog && uiState.resaForJeuDialog != null) {
        val resaId = uiState.resaForJeuDialog.id ?: return
        val alreadyIds = (uiState.reservationJeux[resaId] ?: emptyList()).map { it.jeuId }.toSet()
        val available = remember(uiState.editeurJeuxForDialog, alreadyIds) {
            uiState.editeurJeuxForDialog.filter { it.id !in alreadyIds }
        }
        AddResaJeuDialog(
            jeux = available,
            onAdd = viewModel::addJeuToReservation,
            onDismiss = viewModel::dismissAddResaJeuDialog
        )
    }

    val assignTableId = uiState.resaTableForJeuDialog?.id
    val assignResaId = uiState.resaIdForJeuTableDialog
    if (uiState.showAssignJeuToResaTableDialog && assignTableId != null && assignResaId != null) {
        val alreadyOnTableIds = remember(uiState.resaTableJeux, assignTableId) {
            (uiState.resaTableJeux[assignTableId] ?: emptyList()).map { it.id }.toSet()
        }
        val reservationJeuxByJeuId = remember(uiState.reservationJeux, assignResaId) {
            (uiState.reservationJeux[assignResaId] ?: emptyList()).associateBy { it.jeuId }
        }
        AssignJeuToResaTableDialog(
            editeurJeux = uiState.editeurJeuxForDialog,
            alreadyOnTableIds = alreadyOnTableIds,
            reservationJeuxByJeuId = reservationJeuxByJeuId,
            onAssign = { jeuId -> viewModel.assignJeuToResaTable(jeuId, assignTableId) },
            onDismiss = viewModel::dismissAssignJeuToResaTableDialog
        )
    }

    val filteredReservations = remember(uiState.reservations, uiState.selectedReservationStatut) {
        if (uiState.selectedReservationStatut == null) uiState.reservations
        else uiState.reservations.filter { it.statutWorkflow == uiState.selectedReservationStatut }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header : filter chips + bouton créer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(listOf(null) + StatutWorkflow.entries) { statut ->
                    FilterChip(
                        selected = uiState.selectedReservationStatut == statut,
                        onClick = { viewModel.onReservationStatutFilterChange(statut) },
                        label = { Text(statut?.label ?: "Tous", fontSize = 11.sp) }
                    )
                }
            }
            if (isOrganisateurPlus) {
                Spacer(Modifier.width(8.dp))
                FabAdd(onClick = onCreateReservation)
            }
        }

        if (filteredReservations.isEmpty()) {
            EmptyState("📋", "Aucune réservation")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredReservations, key = { it.id ?: it.editeurId }) { resa ->
                    val resaId = resa.id ?: return@items
                    val isExpanded = resaId in uiState.expandedReservationIds
                    val editeur = uiState.editeurs.firstOrNull { it.id == resa.editeurId }
                    ReservationCard(
                        resa = resa,
                        editeur = editeur,
                        isExpanded = isExpanded,
                        isOrganisateurPlus = isOrganisateurPlus,
                        reservationJeux = uiState.reservationJeux[resaId],
                        reservationTables = uiState.reservationTables[resaId],
                        expandedResaTableIds = uiState.expandedResaTableIds,
                        resaTableJeux = uiState.resaTableJeux,
                        onToggle = { viewModel.toggleReservationExpand(resaId) },
                        onEdit = { onEditReservation(resaId) },
                        onDelete = { resaToDelete = resa },
                        onAddJeu = { viewModel.showAddResaJeuDialog(resa) },
                        onRemoveJeu = { jeuFestivalId -> viewModel.removeJeuFromReservation(jeuFestivalId, resaId) },
                        onAddTable = { viewModel.showAddResaTableDialog(resa) },
                        onRemoveTable = { tableId -> viewModel.removeTableFromReservation(resaId, tableId) },
                        onToggleTable = { tableId -> viewModel.toggleResaTableExpand(tableId) },
                        onAssignJeuToTable = { table -> viewModel.showAssignJeuToResaTableDialog(table, resa) },
                        onRemoveJeuFromTable = { jeuFestivalId, tableId -> viewModel.removeJeuFromResaTable(jeuFestivalId, tableId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    resa: ReservationDto,
    editeur: EditeurDto?,
    isExpanded: Boolean,
    isOrganisateurPlus: Boolean,
    reservationJeux: List<JeuFestivalViewDto>?,
    reservationTables: List<TableJeuDto>?,
    expandedResaTableIds: Set<Int>,
    resaTableJeux: Map<Int, List<JeuTableDto>>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddJeu: () -> Unit,
    onRemoveJeu: (Int) -> Unit,
    onAddTable: () -> Unit,
    onRemoveTable: (Int) -> Unit,
    onToggleTable: (Int) -> Unit,
    onAssignJeuToTable: (TableJeuDto) -> Unit,
    onRemoveJeuFromTable: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Collapsed header
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editeur != null) {
                    if (editeur.logoUrl != null) {
                        AsyncImage(
                            model = editeur.logoUrl, contentDescription = editeur.nom,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                    } else {
                        EditeurAvatar(editeur.nom, size = 36)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        editeur?.nom ?: "Éditeur #${resa.editeurId}",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue
                    )
                    Text("Réservation #${resa.id}", fontSize = 10.sp, color = TextMuted)
                }
                StatutWorkflowBadge(resa.statutWorkflow)
                if (isOrganisateurPlus) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(14.dp))
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextMuted, modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                HorizontalDivider(color = BorderColor)
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Section Prix
                    SectionLabel("Prix")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (resa.prixTotal != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💶", fontSize = 16.sp)
                                Text("${resa.prixTotal}€", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                Text("Total", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        if (resa.prixFinal != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💰", fontSize = 16.sp)
                                Text("${resa.prixFinal}€", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                Text("Final", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        if (resa.remisePourcentage != null && resa.remisePourcentage > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🏷️", fontSize = 16.sp)
                                Text("${resa.remisePourcentage}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                Text("Remise", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Section Jeux
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Jeux", reservationJeux?.size)
                        Spacer(Modifier.weight(1f))
                        if (isOrganisateurPlus) {
                            TextButton(onClick = onAddJeu) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ajouter", fontSize = 12.sp)
                            }
                        }
                    }
                    if (reservationJeux == null) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        reservationJeux.forEach { jeu ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F4F8))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(jeu.jeuNom ?: "Jeu #${jeu.id}", fontSize = 12.sp, color = NavyBlue, modifier = Modifier.weight(1f))
                                if (isOrganisateurPlus) {
                                    IconButton(onClick = { onRemoveJeu(jeu.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Remove, null, tint = Destructive, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Section Tables
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Tables", reservationTables?.size)
                        Spacer(Modifier.weight(1f))
                        if (isOrganisateurPlus) {
                            TextButton(onClick = onAddTable) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Réserver", fontSize = 12.sp)
                            }
                        }
                    }
                    if (reservationTables == null) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        reservationTables.forEach { table ->
                            val tableId = table.id ?: return@forEach
                            val tableExpanded = tableId in expandedResaTableIds
                            ResaTableRow(
                                table = table,
                                isExpanded = tableExpanded,
                                jeux = resaTableJeux[tableId],
                                isOrganisateurPlus = isOrganisateurPlus,
                                onToggle = { onToggleTable(tableId) },
                                onRemove = { onRemoveTable(tableId) },
                                onAssignJeu = { onAssignJeuToTable(table) },
                                onRemoveJeu = { jeuFestivalId -> onRemoveJeuFromTable(jeuFestivalId, tableId) }
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResaTableRow(
    table: TableJeuDto,
    isExpanded: Boolean,
    jeux: List<JeuTableDto>?,
    isOrganisateurPlus: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onAssignJeu: () -> Unit,
    onRemoveJeu: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F4F8))
    ) {
        // En-tête toujours visible
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Partie gauche cliquable pour expand/collapse
            Row(
                modifier = Modifier.weight(1f).clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextMuted, modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Table #${table.id}",
                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = NavyBlue
                )
                Spacer(Modifier.width(8.dp))
                StatutTableBadge(table.statut)
            }
            // Boutons toujours visibles
            if (isOrganisateurPlus) {
                if (table.statut != StatutTable.PLEIN) {
                    IconButton(onClick = onAssignJeu, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Add, "Assigner un jeu", tint = BrightBlue, modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Remove, "Retirer la table", tint = Destructive, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Section dépliable : liste des jeux sur la table
        if (isExpanded) {
            Column(modifier = Modifier.padding(start = 28.dp, end = 8.dp, bottom = 8.dp)) {
                if (jeux == null) {
                    Box(Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                } else if (jeux.isEmpty()) {
                    Text("Aucun jeu sur cette table", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    jeux.forEach { jeu ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(jeu.nom ?: "Jeu #${jeu.id}", fontSize = 11.sp, color = NavyBlue, modifier = Modifier.weight(1f))
                            if (isOrganisateurPlus) {
                                IconButton(onClick = { onRemoveJeu(jeu.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Remove, null, tint = Destructive, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddResaTableDialog(
    zones: List<ZoneDuPlanDto>,
    selectedZoneId: Int?,
    tables: List<TableJeuDto>,
    onSelectZone: (Int) -> Unit,
    onAddTable: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var zoneExpanded by remember { mutableStateOf(false) }
    val selectedZone = zones.firstOrNull { it.id == selectedZoneId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Réserver une table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = selectedZone?.nom ?: "Sélectionner une zone",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Zone du plan") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { zoneExpanded = true }) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
                    )
                    DropdownMenu(expanded = zoneExpanded, onDismissRequest = { zoneExpanded = false }) {
                        zones.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone.nom, fontSize = 13.sp) },
                                onClick = { zone.id?.let(onSelectZone); zoneExpanded = false }
                            )
                        }
                    }
                }
                if (selectedZoneId != null) {
                    if (tables.isEmpty()) {
                        Text("Aucune table libre dans cette zone", fontSize = 12.sp, color = TextMuted)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(tables, key = { it.id ?: it.zoneDuPlanId ?: 0 }) { table ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { table.id?.let(onAddTable) }
                                        .clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F4F8))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Table #${table.id}", fontSize = 13.sp, color = NavyBlue, modifier = Modifier.weight(1f))
                                    Text("${table.nbJeuxActuels ?: 0}/${table.capaciteJeux ?: "?"}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer", color = TextMuted) } }
    )
}

@Composable
private fun AddResaJeuDialog(
    jeux: List<JeuDto>,
    onAdd: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un jeu") },
        text = {
            if (jeux.isEmpty()) {
                Text("Aucun jeu disponible", color = TextMuted, fontSize = 13.sp)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(jeux, key = { it.id ?: it.nom }) { jeu ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { jeu.id?.let(onAdd) }
                                .clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F4F8))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(jeu.nom, fontSize = 13.sp, color = NavyBlue, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer", color = TextMuted) } }
    )
}

@Composable
private fun AssignJeuToResaTableDialog(
    editeurJeux: List<JeuDto>,
    alreadyOnTableIds: Set<Int>,        // jeuFestival IDs déjà sur cette table
    reservationJeuxByJeuId: Map<Int, JeuFestivalViewDto>, // jeuId → jeuFestival
    onAssign: (Int) -> Unit,            // passe le jeuId
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigner un jeu à la table") },
        text = {
            if (editeurJeux.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = BrightBlue)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(editeurJeux, key = { it.id ?: it.nom }) { jeu ->
                        val jeuFestival = jeu.id?.let { reservationJeuxByJeuId[it] }
                        val dejaTable = jeuFestival != null && jeuFestival.id in alreadyOnTableIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (dejaTable) Color(0xFFEEEEEE) else Color(0xFFF0F4F8))
                                .clickable(enabled = !dejaTable) { jeu.id?.let(onAssign) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    jeu.nom,
                                    fontSize = 13.sp,
                                    color = if (dejaTable) TextMuted else NavyBlue,
                                    fontWeight = FontWeight.Medium
                                )
                                when {
                                    dejaTable -> Text("Déjà sur cette table", fontSize = 10.sp, color = TextMuted)
                                    jeuFestival != null -> Text("Dans la réservation", fontSize = 10.sp, color = BrightBlue)
                                    else -> Text("Sera ajouté à la réservation", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }
                            if (dejaTable) {
                                Icon(Icons.Default.Check, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer", color = TextMuted) } }
    )
}
