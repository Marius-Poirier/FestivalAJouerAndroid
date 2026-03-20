package com.example.frontend_mobile_etape1.ui.screens.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend_mobile_etape1.data.dto.*
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import com.example.frontend_mobile_etape1.ui.components.*
import com.example.frontend_mobile_etape1.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowScreen(
    onReservationClick: (Int) -> Unit,
    onAddReservation: (Int) -> Unit,
    onEditeurClick: (Int) -> Unit,
    onJeuClick: (Int) -> Unit = {},
    onEditReservation: (Int) -> Unit = {},
    viewModel: WorkflowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManageSuperOrga = viewModel.authManager.isAdminSuperorga
    val canManageOrga = viewModel.authManager.isAdminSuperorgaOrga

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Workflow")

        // ── Sélecteur de festival ──────────────────────────
        if (uiState.festivals.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        "FESTIVAL COURANT",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        val currentFestivalNom = uiState.festivals
                            .firstOrNull { it.id == uiState.selectedFestivalId }?.nom ?: ""
                        OutlinedTextField(
                            value = currentFestivalNom,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue
                            ),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.festivals.forEach { festival ->
                                DropdownMenuItem(
                                    text = { Text(festival.nom) },
                                    onClick = {
                                        festival.id?.let { viewModel.onFestivalSelected(it) }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Onglets ────────────────────────────────────────
        val visibleTabs = if (canManageSuperOrga) {
            WorkflowTab.entries
        } else {
            WorkflowTab.entries.filter {
                it != WorkflowTab.ZONE_TARIFAIRE && it != WorkflowTab.ZONE_PLAN
            }
        }

        val selectedIndex = visibleTabs.indexOf(uiState.activeTab).coerceAtLeast(0)

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.White,
            contentColor = BrightBlue,
            edgePadding = 0.dp,
            divider = { HorizontalDivider(color = Color(0xFFE8EDF2), thickness = 2.dp) }
        ) {
            visibleTabs.forEach { tab ->
                Tab(
                    selected = uiState.activeTab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    text = {
                        Text(
                            tab.label,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = if (uiState.activeTab == tab) BrightBlue else TextMuted
                        )
                    }
                )
            }
        }

        if (uiState.error != null) {
            ErrorBanner(
                uiState.error!!,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        } else {
            when (uiState.activeTab) {
                WorkflowTab.EDITEUR -> EditeurTab(uiState, viewModel, onEditeurClick)
                WorkflowTab.JEUX -> JeuxTab(uiState, viewModel, onJeuClick)
                WorkflowTab.ZONE_TARIFAIRE -> ZoneTarifaireTab(uiState, viewModel, canManageSuperOrga)
                WorkflowTab.ZONE_PLAN -> ZonePlanTab(uiState, viewModel, canManageSuperOrga, canManageOrga)
                WorkflowTab.RESERVATIONS -> ReservationsTab(
                    uiState, viewModel, canManageOrga, onReservationClick, onAddReservation, onEditReservation
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Tab Éditeur
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditeurTab(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    onEditeurClick: (Int) -> Unit
) {
    val filtered = uiState.editeurs.filter { editeur ->
        val matchSearch = uiState.editeursSearch.isBlank() ||
            editeur.nom.contains(uiState.editeursSearch, ignoreCase = true)
        val reservation = uiState.reservationByEditeur[editeur.id]
        val matchStatut = uiState.selectedStatutFilter == null ||
            (uiState.selectedStatutFilter == StatutWorkflow.PAS_CONTACTE && reservation == null) ||
            reservation?.statutWorkflow == uiState.selectedStatutFilter
        matchSearch && matchStatut
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            SearchBar(
                value = uiState.editeursSearch,
                onValueChange = viewModel::onEditeursSearch,
                placeholder = "Rechercher un éditeur..."
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Éditeurs (${filtered.size} / ${uiState.editeurs.size})",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyBlue
            )
            Spacer(Modifier.height(4.dp))
            StatutWorkflowFilterRow(
                selected = uiState.selectedStatutFilter,
                onSelected = viewModel::onStatutFilter
            )
        }
        if (filtered.isEmpty()) {
            EmptyState("📚", "Aucun éditeur pour ce festival")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id ?: it.nom }) { editeur ->
                    val reservation = uiState.reservationByEditeur[editeur.id]
                    EditeurWorkflowCard(
                        editeur = editeur,
                        reservation = reservation,
                        onClick = { editeur.id?.let(onEditeurClick) },
                        onStatusChange = { newStatut -> 
                            editeur.id?.let { viewModel.updateReservationStatus(it, newStatut) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditeurWorkflowCard(
    editeur: EditeurDto,
    reservation: ReservationDto?,
    onClick: () -> Unit,
    onStatusChange: (StatutWorkflow) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditeurAvatar(editeur.nom, editeur.logoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(editeur.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                Spacer(Modifier.height(4.dp))
                
                Box {
                    StatutWorkflowBadge(
                        statut = reservation?.statutWorkflow ?: StatutWorkflow.PAS_CONTACTE,
                        modifier = Modifier.clickable { expanded = true }
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        StatutWorkflow.entries.forEach { statut ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(statut.dotColor))
                                        Text(statut.label, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    onStatusChange(statut)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Tab Jeux
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun JeuxTab(uiState: WorkflowUiState, viewModel: WorkflowViewModel, onJeuClick: (Int) -> Unit) {
    val filtered = uiState.jeuxFestival.filter {
        uiState.jeuxSearch.isBlank() || it.jeuNom?.contains(uiState.jeuxSearch, ignoreCase = true) == true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            SearchBar(
                value = uiState.jeuxSearch,
                onValueChange = viewModel::onJeuxSearch,
                placeholder = "Rechercher un jeu..."
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Jeux (${filtered.size} / ${uiState.jeuxFestival.size})",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyBlue
            )
        }
        if (filtered.isEmpty()) {
            EmptyState("🎲", "Aucun jeu dans ce festival")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { jeuFestival ->
                    JeuFestivalCard(jeuFestival, onClick = { jeuFestival.jeuId?.let(onJeuClick) })
                }
            }
        }
    }
}

@Composable
private fun JeuFestivalCard(jeu: JeuFestivalViewDto, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(75.dp)) {
                if (jeu.urlImage != null) {
                    AsyncImage(
                        model = jeu.urlImage,
                        contentDescription = jeu.jeuNom,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFDDE3EA)),
                        contentAlignment = Alignment.Center
                    ) { Text("🖼️", fontSize = 20.sp) }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    jeu.jeuNom ?: "", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrightBlue,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (jeu.typeJeuNom != null) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDBEAFE))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(jeu.typeJeuNom, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }
                }
                val info = buildString {
                    if (jeu.nbJoueursMin != null && jeu.nbJoueursMax != null)
                        append("👥 ${jeu.nbJoueursMin}–${jeu.nbJoueursMax}")
                    jeu.dureeMinutes?.let { append(" · ⏱ ~$it min") }
                }
                if (info.isNotEmpty()) {
                    Text(info, fontSize = 9.sp, color = Color(0xFF555555))
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(CardSecondary)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    jeu.editeurNom ?: "", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Tab Zone Tarifaire
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZoneTarifaireTab(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    canManage: Boolean
) {
    var zoneToDelete by remember { mutableStateOf<ZoneTarifaireDto?>(null) }
    if (zoneToDelete != null) {
        ConfirmDeleteDialog(
            itemName = zoneToDelete!!.nom,
            onConfirm = { zoneToDelete!!.id?.let { viewModel.deleteZoneTarifaire(it) }; zoneToDelete = null },
            onDismiss = { zoneToDelete = null }
        )
    }

    if (uiState.showZoneTarifaireDialog) {
        ZoneTarifaireDialog(
            editing = uiState.editingZoneTarifaire,
            onSave = { nom, nbTables, prixTable, prixM2 ->
                viewModel.saveZoneTarifaire(nom, nbTables, prixTable, prixM2)
            },
            onDismiss = { viewModel.dismissZoneTarifaireDialog() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zones tarifaires", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
            if (canManage) {
                FabAdd(onClick = { viewModel.openZoneTarifaireDialog() })
            }
        }
        if (uiState.zonesTarifaires.isEmpty()) {
            EmptyState("🏷️", "Aucune zone tarifaire")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.zonesTarifaires, key = { it.id ?: it.nom }) { zone ->
                    ZoneTarifaireCard(
                        zone = zone,
                        canManage = canManage,
                        onEdit = { viewModel.openZoneTarifaireDialog(zone) },
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                if (canManage) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatBox("💶", "Prix/table", "%.2f€".format(zone.prixTable), Modifier.weight(1f))
                StatBox("📐", "Prix/m²", "%.2f€".format(zone.prixM2), Modifier.weight(1f))
                StatBox("📋", "Tables", "${zone.nombreTablesTotal}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ZoneTarifaireDialog(
    editing: ZoneTarifaireDto?,
    onSave: (String, Int, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var nom by remember { mutableStateOf(editing?.nom ?: "") }
    var nombreTables by remember { mutableStateOf(editing?.nombreTablesTotal?.toString() ?: "") }
    var prixTable by remember { mutableStateOf(editing?.prixTable?.toString() ?: "") }
    var prixM2 by remember { mutableStateOf(editing?.prixM2?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (editing != null) "Modifier la zone tarifaire" else "Nouvelle zone tarifaire",
                fontWeight = FontWeight.Bold, fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = nombreTables,
                    onValueChange = { nombreTables = it },
                    label = { Text("Nombre de tables total", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = prixTable,
                    onValueChange = { prixTable = it },
                    label = { Text("Prix par table (€)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = prixM2,
                    onValueChange = { prixM2 = it },
                    label = { Text("Prix par m² (€)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nb = nombreTables.toIntOrNull() ?: return@TextButton
                    val pt = prixTable.toDoubleOrNull() ?: return@TextButton
                    val pm = prixM2.toDoubleOrNull() ?: return@TextButton
                    if (nom.isNotBlank()) onSave(nom.trim(), nb, pt, pm)
                }
            ) {
                Text(if (editing != null) "Modifier" else "Créer", fontWeight = FontWeight.Bold, color = BrightBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Tab Zone du Plan
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZonePlanTab(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    canManage: Boolean,
    canManageOrga: Boolean
) {
    var zoneToDelete by remember { mutableStateOf<ZoneDuPlanDto?>(null) }
    if (zoneToDelete != null) {
        ConfirmDeleteDialog(
            itemName = zoneToDelete!!.nom,
            onConfirm = { zoneToDelete!!.id?.let { viewModel.deleteZoneDuPlan(it) }; zoneToDelete = null },
            onDismiss = { zoneToDelete = null }
        )
    }

    if (uiState.showZonePlanDialog) {
        ZonePlanDialog(
            editing = uiState.editingZonePlan,
            zonesTarifaires = uiState.zonesTarifaires,
            onSave = { nom, zoneTarifaireId -> viewModel.saveZoneDuPlan(nom, zoneTarifaireId) },
            onDismiss = { viewModel.dismissZonePlanDialog() }
        )
    }

    if (uiState.showAddTableDialog && uiState.addTableForZone != null) {
        AddTableDialog(
            zone = uiState.addTableForZone,
            onSave = { capacite -> viewModel.addTable(uiState.addTableForZone, capacite) },
            onDismiss = { viewModel.dismissAddTableDialog() }
        )
    }

    if (uiState.showAssignJeuDialog && uiState.assignJeuForTable != null) {
        val tableId = uiState.assignJeuForTable.id
        val assignedJeuIds = if (tableId != null) {
            uiState.jeusByTable[tableId]?.map { it.id }?.toSet() ?: emptySet()
        } else emptySet()
        val availableJeux = uiState.jeuxFestival.filter { it.jeuId !in assignedJeuIds }
        AssignJeuDialog(
            availableJeux = availableJeux,
            onAssign = { jeuFestivalId ->
                tableId?.let { viewModel.assignJeuToTable(jeuFestivalId, it) }
            },
            onDismiss = { viewModel.dismissAssignJeuDialog() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zones du plan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
            if (canManage) {
                FabAdd(onClick = { viewModel.openZonePlanDialog() })
            }
        }
        if (uiState.zonesDuPlan.isEmpty()) {
            EmptyState("🗺️", "Aucune zone du plan")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.zonesDuPlan, key = { it.id ?: it.nom }) { zone ->
                    val zoneTarifaire = uiState.zonesTarifaires.firstOrNull { it.id == zone.zoneTarifaireId }
                    val isExpanded = zone.id != null && zone.id in uiState.expandedZonePlanIds
                    val tables = if (zone.id != null) uiState.tablesByZone[zone.id] else null
                    ZonePlanCard(
                        zone = zone,
                        zoneTarifaire = zoneTarifaire,
                        canManage = canManage,
                        canManageOrga = canManageOrga,
                        isExpanded = isExpanded,
                        tables = tables,
                        expandedTableIds = uiState.expandedTableIds,
                        jeusByTable = uiState.jeusByTable,
                        onToggleExpand = { viewModel.toggleZonePlanExpand(zone) },
                        onEdit = { viewModel.openZonePlanDialog(zone) },
                        onDelete = { zoneToDelete = zone },
                        onAddTable = { viewModel.openAddTableDialog(zone) },
                        onDeleteTable = { tableId -> zone.id?.let { viewModel.deleteTable(tableId, it) } },
                        onToggleTable = { table -> viewModel.toggleTableExpand(table) },
                        onAssignJeu = { table -> viewModel.openAssignJeuDialog(table) },
                        onUnassignJeu = { jeuId, tableId ->
                            zone.id?.let { viewModel.unassignJeuFromTable(jeuId, tableId, it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZonePlanCard(
    zone: ZoneDuPlanDto,
    zoneTarifaire: ZoneTarifaireDto?,
    canManage: Boolean,
    canManageOrga: Boolean,
    isExpanded: Boolean,
    tables: List<TableJeuDto>?,
    expandedTableIds: Set<Int>,
    jeusByTable: Map<Int, List<JeuTableDto>>,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddTable: () -> Unit,
    onDeleteTable: (Int) -> Unit,
    onToggleTable: (TableJeuDto) -> Unit,
    onAssignJeu: (TableJeuDto) -> Unit,
    onUnassignJeu: (jeuId: Int, tableId: Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── En-tête de la zone ──────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue,
                    modifier = Modifier.weight(1f))
                if (canManage) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatBox("📋", "Tables", "${zone.nombreTables}", Modifier.weight(1f))
                if (zoneTarifaire != null) {
                    StatBox("💶", "Prix/table", "%.2f€".format(zoneTarifaire.prixTable), Modifier.weight(1f))
                    StatBox("📐", "Prix/m²", "%.2f€".format(zoneTarifaire.prixM2), Modifier.weight(1f))
                }
            }
            if (zoneTarifaire != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSecondary)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🏷️", fontSize = 12.sp)
                        Text("Zone tarifaire", fontSize = 9.sp, color = TextMuted)
                    }
                    Text(zoneTarifaire.nom, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrightBlue)
                }
            }

            // ── Bouton tables (toggle) ──────────────────────
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand() }
                    .background(if (isExpanded) Color(0xFFDBEAFE) else Color(0xFFF3F4F6))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📋 Tables (${tables?.size ?: zone.nombreTables})",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = if (isExpanded) BrightBlue else NavyBlue
                )
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isExpanded) BrightBlue else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // ── Liste des tables (si déplié) ────────────────
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                if (canManage) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onAddTable,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ Ajouter une table", fontSize = 11.sp, color = BrightBlue,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (tables == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrightBlue)
                    }
                } else if (tables.isEmpty()) {
                    Text(
                        "Aucune table dans cette zone",
                        fontSize = 11.sp, color = TextMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tables.forEach { table ->
                            val tableId = table.id ?: return@forEach
                            val tableExpanded = tableId in expandedTableIds
                            val jeuxDansTable = jeusByTable[tableId]
                            TableRow(
                                table = table,
                                canManage = canManage,
                                canManageOrga = canManageOrga,
                                isExpanded = tableExpanded,
                                jeux = jeuxDansTable,
                                onToggle = { onToggleTable(table) },
                                onDelete = { onDeleteTable(tableId) },
                                onAssignJeu = { onAssignJeu(table) },
                                onUnassignJeu = { jeuId -> onUnassignJeu(jeuId, tableId) }
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
    canManage: Boolean,
    canManageOrga: Boolean,
    isExpanded: Boolean,
    jeux: List<JeuTableDto>?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onAssignJeu: () -> Unit,
    onUnassignJeu: (Int) -> Unit
) {
    val current = table.nbJeuxActuels ?: 0
    val cap = table.capaciteJeux
    val isFull = current >= cap

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
    ) {
        // En-tête de la table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)) {
                Text("#${table.id}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFull) Color(0xFFFEE2E2) else Color(0xFFD1FAE5))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$current/$cap jeux",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = if (isFull) Destructive else Color(0xFF065F46)
                    )
                }
                table.statut?.let { statut ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8EDF2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(statut.label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canManage) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(12.dp))
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Jeux assignés (si déplié)
        if (isExpanded) {
            HorizontalDivider(color = Color(0xFFE8EDF2))
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (jeux == null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrightBlue)
                    }
                } else if (jeux.isEmpty()) {
                    Text("Aucun jeu assigné", fontSize = 10.sp, color = TextMuted)
                } else {
                    jeux.forEach { jeu ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(jeu.nom ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (!jeu.editeurs.isNullOrBlank()) {
                                    Text(jeu.editeurs, fontSize = 9.sp, color = TextMuted,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            if (canManageOrga) {
                                IconButton(onClick = { onUnassignJeu(jeu.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Destructive,
                                        modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                if (canManageOrga && !isFull) {
                    TextButton(
                        onClick = onAssignJeu,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("+ Assigner un jeu", fontSize = 10.sp, color = BrightBlue,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonePlanDialog(
    editing: ZoneDuPlanDto?,
    zonesTarifaires: List<ZoneTarifaireDto>,
    onSave: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var nom by remember { mutableStateOf(editing?.nom ?: "") }
    var selectedZoneTarifaireId by remember {
        mutableStateOf(editing?.zoneTarifaireId ?: zonesTarifaires.firstOrNull()?.id ?: 0)
    }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedZoneTarifaire = zonesTarifaires.firstOrNull { it.id == selectedZoneTarifaireId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (editing != null) "Modifier la zone du plan" else "Nouvelle zone du plan",
                fontWeight = FontWeight.Bold, fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedZoneTarifaire?.nom ?: "Choisir une zone tarifaire",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Zone tarifaire", fontSize = 12.sp) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        zonesTarifaires.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone.nom) },
                                onClick = {
                                    selectedZoneTarifaireId = zone.id ?: 0
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nom.isNotBlank() && selectedZoneTarifaireId > 0) {
                        onSave(nom.trim(), selectedZoneTarifaireId)
                    }
                }
            ) {
                Text(if (editing != null) "Modifier" else "Créer", fontWeight = FontWeight.Bold, color = BrightBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun AddTableDialog(
    zone: ZoneDuPlanDto,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var capacite by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajouter une table", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Zone : ${zone.nom}", fontSize = 12.sp, color = TextMuted)
                OutlinedTextField(
                    value = capacite,
                    onValueChange = { capacite = it },
                    label = { Text("Capacité (nb jeux par table)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cap = capacite.toIntOrNull() ?: return@TextButton
                    if (cap >= 1) onSave(cap)
                }
            ) {
                Text("Ajouter", fontWeight = FontWeight.Bold, color = BrightBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun AssignJeuDialog(
    availableJeux: List<JeuFestivalViewDto>,
    onAssign: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigner un jeu", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            if (availableJeux.isEmpty()) {
                Text("Aucun jeu disponible à assigner.", fontSize = 12.sp, color = TextMuted)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableJeux, key = { it.id }) { jeu ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAssign(jeu.id) }
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(jeu.jeuNom ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(jeu.editeurNom ?: "", fontSize = 10.sp, color = TextMuted,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Tab Réservations
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationsTab(
    uiState: WorkflowUiState,
    viewModel: WorkflowViewModel,
    canManage: Boolean,
    onReservationClick: (Int) -> Unit,
    onAddReservation: (Int) -> Unit,
    onEditReservation: (Int) -> Unit
) {
    val filtered = uiState.reservations.filter { resa ->
        uiState.selectedReservationStatut == null ||
            resa.statutWorkflow == uiState.selectedReservationStatut
    }
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

    // Dialog : réserver une table pour une réservation
    if (uiState.showAddResaTableDialog && uiState.resaForTableDialog != null) {
        AddResaTableDialog(
            zonesDuPlan = uiState.zonesDuPlan,
            freeTablesByZone = uiState.freeTablesByZone,
            onLoadFreeTables = viewModel::loadFreeTablesForZone,
            onAdd = { tableId ->
                uiState.resaForTableDialog.id?.let { viewModel.addTableToReservation(it, tableId) }
            },
            onDismiss = viewModel::dismissAddResaTableDialog
        )
    }

    // Dialog : ajouter un jeu à une réservation
    if (uiState.showAddResaJeuDialog && uiState.resaForJeuDialog != null) {
        val resaId = uiState.resaForJeuDialog.id
        val alreadyInResa = if (resaId != null) {
            uiState.reservationJeux[resaId]?.map { it.jeuId }?.toSet() ?: emptySet()
        } else emptySet()
        AddResaJeuDialog(
            editeurJeux = uiState.editeurJeuxForDialog,
            alreadyInResa = alreadyInResa,
            onAdd = { jeuId -> viewModel.addJeuToReservation(uiState.resaForJeuDialog, jeuId) },
            onDismiss = viewModel::dismissAddResaJeuDialog
        )
    }

    // Dialog : assigner un jeu à une table dans le contexte d'une réservation
    if (uiState.showAssignJeuToResaTableDialog &&
        uiState.resaTableForJeuDialog != null &&
        uiState.resaIdForJeuTableDialog != null
    ) {
        val tableId = uiState.resaTableForJeuDialog.id
        val alreadyOnTable = if (tableId != null) {
            uiState.resaTableJeux[tableId]?.map { it.id }?.toSet() ?: emptySet()
        } else emptySet()
        val available = (uiState.reservationJeux[uiState.resaIdForJeuTableDialog] ?: emptyList())
            .filter { it.jeuId !in alreadyOnTable }
        AssignJeuToResaTableDialog(
            jeuxInResa = available,
            onAssign = { jeuFestivalId ->
                tableId?.let { viewModel.assignJeuToResaTable(jeuFestivalId, it) }
            },
            onDismiss = viewModel::dismissAssignJeuToResaTableDialog
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Réservations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                if (canManage && uiState.selectedFestivalId != null) {
                    FabAdd(onClick = { onAddReservation(uiState.selectedFestivalId) })
                }
            }
            Spacer(Modifier.height(6.dp))
            StatutWorkflowFilterRow(
                selected = uiState.selectedReservationStatut,
                onSelected = viewModel::onReservationStatutFilter
            )
        }
        if (filtered.isEmpty()) {
            EmptyState("📋", "Aucune réservation")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id ?: 0 }) { resa ->
                    val resaId = resa.id ?: return@items
                    val editeur = uiState.editeurs.firstOrNull { it.id == resa.editeurId }
                    val isExpanded = resaId in uiState.expandedReservationIds
                    ReservationWorkflowCard(
                        reservation = resa,
                        editeur = editeur,
                        canManage = canManage,
                        isExpanded = isExpanded,
                        jeux = uiState.reservationJeux[resaId],
                        tables = uiState.reservationTables[resaId],
                        expandedTableIds = uiState.expandedResaTableIds,
                        tableJeux = uiState.resaTableJeux,
                        zonesDuPlan = uiState.zonesDuPlan,
                        onClick = { onReservationClick(resaId) },
                        onEdit = { onEditReservation(resaId) },
                        onDelete = { resaToDelete = resa },
                        onToggleExpand = { viewModel.toggleReservationExpand(resa) },
                        onAddJeu = { viewModel.openAddResaJeuDialog(resa) },
                        onRemoveJeu = { jeuFestivalId ->
                            viewModel.removeJeuFromReservation(jeuFestivalId, resaId, resa.festivalId)
                        },
                        onAddTable = { viewModel.openAddResaTableDialog(resa) },
                        onRemoveTable = { tableId -> viewModel.removeTableFromReservation(resaId, tableId) },
                        onToggleTable = { tableId -> viewModel.toggleResaTableExpand(tableId) },
                        onAssignJeuToTable = { table ->
                            viewModel.openAssignJeuToResaTableDialog(table, resaId)
                        },
                        onUnassignJeuFromTable = { jeuId, tableId ->
                            viewModel.unassignJeuFromResaTable(jeuId, tableId, resaId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationWorkflowCard(
    reservation: ReservationDto,
    editeur: EditeurDto?,
    canManage: Boolean,
    isExpanded: Boolean,
    jeux: List<JeuFestivalViewDto>?,
    tables: List<TableJeuDto>?,
    expandedTableIds: Set<Int>,
    tableJeux: Map<Int, List<JeuTableDto>>,
    zonesDuPlan: List<ZoneDuPlanDto>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleExpand: () -> Unit,
    onAddJeu: () -> Unit,
    onRemoveJeu: (Int) -> Unit,
    onAddTable: () -> Unit,
    onRemoveTable: (Int) -> Unit,
    onToggleTable: (Int) -> Unit,
    onAssignJeuToTable: (TableJeuDto) -> Unit,
    onUnassignJeuFromTable: (jeuId: Int, tableId: Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── En-tête ───────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "RÉSERVATION #${reservation.id}",
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = TextMuted, letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f).clickable { onClick() }
                )
                if (canManage) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, tint = BrightBlue, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // ── Éditeur + statut + toggle ─────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (editeur != null) {
                    EditeurAvatar(editeur.nom, editeur.logoUrl)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        editeur.nom,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                StatutWorkflowBadge(reservation.statutWorkflow)
                IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Contenu étendu ────────────────────────────────────────────────
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE8EDF2))
                Spacer(Modifier.height(8.dp))

                // Prix
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    reservation.prixTotal?.let {
                        StatBox("💶", "Total", "%.2f€".format(it), Modifier.weight(1f))
                    }
                    reservation.prixFinal?.let {
                        StatBox("💰", "Final", "%.2f€".format(it), Modifier.weight(1f))
                    }
                    reservation.remisePourcentage?.let { remise ->
                        if (remise > 0) StatBox("🏷️", "Remise", "${remise}%", Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE8EDF2))
                Spacer(Modifier.height(6.dp))

                // ── Jeux de la réservation ────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🎲 Jeux (${jeux?.size ?: "…"})",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue
                    )
                    if (canManage) {
                        TextButton(
                            onClick = onAddJeu,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ Ajouter", fontSize = 10.sp, color = BrightBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                when {
                    jeux == null -> Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrightBlue) }
                    jeux.isEmpty() -> Text(
                        "Aucun jeu lié à cette réservation",
                        fontSize = 10.sp, color = TextMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        jeux.forEach { jeu ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    jeu.jeuNom ?: "",
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                if (canManage) {
                                    IconButton(onClick = { onRemoveJeu(jeu.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE8EDF2))
                Spacer(Modifier.height(6.dp))

                // ── Tables réservées ──────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📋 Tables (${tables?.size ?: "…"})",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue
                    )
                    if (canManage) {
                        TextButton(
                            onClick = onAddTable,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ Réserver", fontSize = 10.sp, color = BrightBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                when {
                    tables == null -> Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrightBlue) }
                    tables.isEmpty() -> Text(
                        "Aucune table réservée",
                        fontSize = 10.sp, color = TextMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tables.forEach { table ->
                            val tableId = table.id ?: return@forEach
                            val tableExpanded = tableId in expandedTableIds
                            val zone = zonesDuPlan.firstOrNull { it.id == table.zoneDuPlanId }
                            ResaTableRow(
                                table = table,
                                zone = zone,
                                isExpanded = tableExpanded,
                                jeux = tableJeux[tableId],
                                canManage = canManage,
                                onToggle = { onToggleTable(tableId) },
                                onRemove = { onRemoveTable(tableId) },
                                onAssignJeu = { onAssignJeuToTable(table) },
                                onUnassignJeu = { jeuId -> onUnassignJeuFromTable(jeuId, tableId) }
                            )
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
    zone: ZoneDuPlanDto?,
    isExpanded: Boolean,
    jeux: List<JeuTableDto>?,
    canManage: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onAssignJeu: () -> Unit,
    onUnassignJeu: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    zone?.nom ?: "Table #${table.id}",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue
                )
                val cap = table.capaciteJeux
                val current = table.nbJeuxActuels ?: 0
                Text(
                    "$current/$cap jeux · ${table.statut?.label ?: ""}",
                    fontSize = 9.sp, color = TextMuted
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canManage) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(12.dp))
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (isExpanded) {
            HorizontalDivider(color = Color(0xFFE8EDF2))
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when {
                    jeux == null -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrightBlue) }
                    jeux.isEmpty() -> Text("Aucun jeu sur cette table", fontSize = 10.sp, color = TextMuted)
                    else -> jeux.forEach { jeu ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                jeu.nom ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                modifier = Modifier.weight(1f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            if (canManage) {
                                IconButton(onClick = { onUnassignJeu(jeu.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
                if (canManage) {
                    TextButton(
                        onClick = onAssignJeu,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("+ Assigner un jeu", fontSize = 10.sp, color = BrightBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddResaTableDialog(
    zonesDuPlan: List<ZoneDuPlanDto>,
    freeTablesByZone: Map<Int, List<TableJeuDto>>,
    onLoadFreeTables: (Int) -> Unit,
    onAdd: (tableId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedZoneId by remember { mutableStateOf<Int?>(null) }
    var selectedTableId by remember { mutableStateOf<Int?>(null) }
    var zoneExpanded by remember { mutableStateOf(false) }
    var tableExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Réserver une table", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (zonesDuPlan.isEmpty()) {
                    Text("Chargement des zones…", fontSize = 12.sp, color = TextMuted)
                } else {
                    Text("Zone du plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    ExposedDropdownMenuBox(expanded = zoneExpanded, onExpandedChange = { zoneExpanded = it }) {
                        OutlinedTextField(
                            value = zonesDuPlan.firstOrNull { it.id == selectedZoneId }?.nom ?: "Sélectionner une zone…",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(zoneExpanded) }
                        )
                        ExposedDropdownMenu(expanded = zoneExpanded, onDismissRequest = { zoneExpanded = false }) {
                            zonesDuPlan.forEach { zone ->
                                DropdownMenuItem(
                                    text = { Text(zone.nom) },
                                    onClick = {
                                        selectedZoneId = zone.id
                                        selectedTableId = null
                                        zoneExpanded = false
                                        zone.id?.let { onLoadFreeTables(it) }
                                    }
                                )
                            }
                        }
                    }
                    if (selectedZoneId != null) {
                        val freeTables = freeTablesByZone[selectedZoneId] ?: emptyList()
                        Text("Table disponible", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        if (freeTables.isEmpty()) {
                            Text("Aucune table libre dans cette zone", fontSize = 11.sp, color = TextMuted)
                        } else {
                            ExposedDropdownMenuBox(expanded = tableExpanded, onExpandedChange = { tableExpanded = it }) {
                                OutlinedTextField(
                                    value = freeTables.firstOrNull { it.id == selectedTableId }
                                        ?.let { "Table #${it.id} (${it.capaciteJeux} jeux)" }
                                        ?: "Sélectionner une table…",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tableExpanded) }
                                )
                                ExposedDropdownMenu(expanded = tableExpanded, onDismissRequest = { tableExpanded = false }) {
                                    freeTables.forEach { table ->
                                        DropdownMenuItem(
                                            text = { Text("Table #${table.id} (cap. ${table.capaciteJeux} jeux)") },
                                            onClick = { selectedTableId = table.id; tableExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedTableId?.let { onAdd(it) } },
                enabled = selectedTableId != null
            ) {
                Text("Réserver", fontWeight = FontWeight.Bold, color = BrightBlue)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun AddResaJeuDialog(
    editeurJeux: List<JeuDto>,
    alreadyInResa: Set<Int>,
    onAdd: (jeuId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val available = editeurJeux.filter { it.id != null && it.id !in alreadyInResa }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un jeu", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            when {
                editeurJeux.isEmpty() -> Text("Chargement…", fontSize = 12.sp, color = TextMuted)
                available.isEmpty() -> Text(
                    "Tous les jeux de cet éditeur sont déjà dans la réservation.",
                    fontSize = 12.sp, color = TextMuted
                )
                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(available, key = { it.id ?: it.nom }) { jeu ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { jeu.id?.let { onAdd(it) } }
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                jeu.nom, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
private fun AssignJeuToResaTableDialog(
    jeuxInResa: List<JeuFestivalViewDto>,
    onAssign: (jeuFestivalId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigner un jeu à la table", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            if (jeuxInResa.isEmpty()) {
                Text(
                    "Aucun jeu disponible (tous déjà sur cette table).",
                    fontSize = 12.sp, color = TextMuted
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(jeuxInResa, key = { it.id }) { jeu ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAssign(jeu.id) }
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    jeu.jeuNom ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    jeu.editeurNom ?: "", fontSize = 10.sp, color = TextMuted,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Filtre statut partagé
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatutWorkflowFilterRow(
    selected: StatutWorkflow?,
    onSelected: (StatutWorkflow?) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text("Tous", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrightBlue, selectedLabelColor = Color.White
            )
        )
        StatutWorkflow.entries.forEach { statut ->
            FilterChip(
                selected = selected == statut,
                onClick = { onSelected(if (selected == statut) null else statut) },
                label = { Text(statut.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected == statut) Color.White else statut.dotColor)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrightBlue, selectedLabelColor = Color.White
                )
            )
        }
    }
}
