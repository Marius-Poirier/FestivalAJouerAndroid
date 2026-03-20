package com.example.frontend_mobile_etape1.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.frontend_mobile_etape1.data.dto.*
import com.example.frontend_mobile_etape1.data.enums.StatutTable
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import com.example.frontend_mobile_etape1.ui.components.*
import com.example.frontend_mobile_etape1.ui.theme.*

@Composable
fun ReservationDetailScreen(
    reservationId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ReservationDetailViewModel = viewModel(
        key = "res_detail_$reservationId",
        factory = viewModelFactory { initializer { ReservationDetailViewModel(reservationId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            itemName = "Réservation #$reservationId",
            onConfirm = { viewModel.delete { onBack() } },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = "Réservation #$reservationId",
            showBackButton = true,
            onBackClick = onBack,
            actions = {
                val resa = uiState.reservation
                if (resa != null) {
                    if (viewModel.authManager.isAdminSuperorgaOrga) {
                        val isPaiementRecuBlocked = resa.statutWorkflow == StatutWorkflow.PAIEMENT_RECU &&
                            !viewModel.authManager.isAdminSuperorga
                        IconButton(
                            onClick = { if (!isPaiementRecuBlocked) resa.id?.let(onEdit) },
                            enabled = !isPaiementRecuBlocked
                        ) {
                            Icon(
                                imageVector = if (isPaiementRecuBlocked) Icons.Default.Lock else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isPaiementRecuBlocked) Color.White.copy(alpha = 0.4f) else Color.White
                            )
                        }
                    }
                    if (viewModel.canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
                        }
                    }
                }
            }
        )

        if (uiState.isLoading) { LoadingOverlay(); return@Column }
        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(16.dp))
            return@Column
        }

        uiState.reservation ?: return@Column

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Infos") })
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Jeux")
                        if (uiState.jeuxFestival.isNotEmpty()) {
                            CountBadge(uiState.jeuxFestival.size)
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tables")
                        if (uiState.tables.isNotEmpty()) {
                            CountBadge(uiState.tables.size)
                        }
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> InfosTab(uiState)
            1 -> JeuxTab(uiState, viewModel)
            2 -> TablesTab(uiState, viewModel)
        }
    }
}

// ─────────────────────────────────────────────────────
// Badge compteur (remplace Badge expérimental)
// ─────────────────────────────────────────────────────

@Composable
private fun CountBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text("$count", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────
// Onglet Infos
// ─────────────────────────────────────────────────────

@Composable
private fun InfosTab(uiState: ReservationDetailUiState) {
    val resa = uiState.reservation ?: return

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val editeur = uiState.editeur
                    if (editeur != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EditeurAvatar(editeur.nom, editeur.logoUrl, size = 48)
                            Column {
                                Text(editeur.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                                Text("Festival #${resa.festivalId}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    StatutWorkflowBadge(resa.statutWorkflow)
                    Spacer(Modifier.height(12.dp))

                    if (resa.prixTotal != null || resa.prixFinal != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            resa.prixTotal?.let { StatBox("💰", "Prix total", "%.2f€".format(it), Modifier.weight(1f)) }
                            resa.prixFinal?.let { StatBox("✅", "Prix final", "%.2f€".format(it), Modifier.weight(1f)) }
                            resa.remisePourcentage?.let { StatBox("🏷", "Remise", "%.0f%%".format(it), Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (!resa.commentairesPaiement.isNullOrBlank()) {
                        Text("Commentaires paiement", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted)
                        Text(resa.commentairesPaiement, fontSize = 12.sp, color = Color(0xFF333333))
                        Spacer(Modifier.height(4.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlagBadge("Présente jeux", resa.editeurPresenteJeux)
                        FlagBadge("Relance paiement", resa.paiementRelance)
                    }

                    if (resa.dateFacture != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("Facturé le : ${resa.dateFacture}", fontSize = 11.sp, color = TextMuted)
                    }
                    if (resa.datePaiement != null) {
                        Text("Payé le : ${resa.datePaiement}", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────
// Onglet Jeux
// ─────────────────────────────────────────────────────

@Composable
private fun JeuxTab(uiState: ReservationDetailUiState, viewModel: ReservationDetailViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    val alreadyLinkedIds = remember(uiState.jeuxFestival) { uiState.jeuxFestival.map { it.jeuId }.toSet() }
    val availableJeux = remember(uiState.editeurJeux, alreadyLinkedIds) {
        uiState.editeurJeux.filter { it.id != null && !alreadyLinkedIds.contains(it.id) }
    }

    if (showAddDialog) {
        AddJeuDialog(
            availableJeux = availableJeux,
            onConfirm = { jeuId -> viewModel.addJeu(jeuId); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jeux (${uiState.jeuxFestival.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
            if (viewModel.canManageJeux) {
                FilledTonalButton(onClick = { showAddDialog = true }, enabled = availableJeux.isNotEmpty()) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ajouter", fontSize = 12.sp)
                }
            }
        }

        if (uiState.jeuxFestival.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun jeu ajouté pour cette réservation.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.jeuxFestival, key = { it.id }) { jeu ->
                    JeuReservationCard(
                        jeu = jeu,
                        canRemove = viewModel.canManageJeux,
                        onRemove = { viewModel.removeJeu(jeu.id) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────
// Onglet Tables
// ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablesTab(uiState: ReservationDetailUiState, viewModel: ReservationDetailViewModel) {
    var tableToRemove by remember { mutableStateOf<Int?>(null) }
    var showAddTableDialog by remember { mutableStateOf(false) }

    if (tableToRemove != null) {
        AlertDialog(
            onDismissRequest = { tableToRemove = null },
            title = { Text("Retirer la table ?") },
            text = { Text("Êtes-vous sûr de vouloir retirer cette table de la réservation ?") },
            confirmButton = {
                TextButton(onClick = { viewModel.removeTable(tableToRemove!!); tableToRemove = null }) {
                    Text("Retirer", color = Color(0xFFEF4444))
                }
            },
            dismissButton = { TextButton(onClick = { tableToRemove = null }) { Text("Annuler") } }
        )
    }

    if (showAddTableDialog) {
        AddResaTableDialog(
            zonesDuPlan = uiState.zonesDuPlan,
            freeTablesByZone = uiState.freeTablesByZone,
            onLoadFreeTables = viewModel::loadFreeTablesForZone,
            onAdd = { tableId ->
                viewModel.addTable(tableId)
                showAddTableDialog = false
            },
            onDismiss = { showAddTableDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tables (${uiState.tables.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
            if (viewModel.canManageTables) {
                FilledTonalButton(onClick = { showAddTableDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Réserver", fontSize = 12.sp)
                }
            }
        }

        if (uiState.tables.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune table attribuée.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.tables, key = { it.id ?: 0 }) { table ->
                    TableReservationCard(
                        table = table,
                        jeux = uiState.tableJeux[table.id],
                        canRemove = viewModel.canManageTables,
                        onRemove = { table.id?.let { tableToRemove = it } },
                        onExpand = { table.id?.let { viewModel.loadTableJeux(it) } },
                        onAssignJeu = { viewModel.openAssignJeuToTable(table) },
                        onUnassignJeu = { jeuId -> table.id?.let { viewModel.unassignJeuFromTable(jeuId, it) } }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // Dialog assignation jeu à table
    if (uiState.showAssignJeuToTableDialog && uiState.resaTableForJeuDialog != null) {
        val tableId = uiState.resaTableForJeuDialog!!.id
        val alreadyOnTable = if (tableId != null) {
            uiState.tableJeux[tableId]?.map { it.id }?.toSet() ?: emptySet()
        } else emptySet()
        val available = uiState.jeuxFestival.filter { it.jeuId !in alreadyOnTable }

        AssignJeuToResaTableDialog(
            jeuxInResa = available,
            onAssign = { jeuFestivalId ->
                tableId?.let { viewModel.assignJeuToTable(jeuFestivalId, it) }
            },
            onDismiss = viewModel::dismissAssignJeuToTableDialog
        )
    }
}

@Composable
private fun TableReservationCard(
    table: TableJeuDto,
    jeux: List<JeuTableDto>?,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onExpand: () -> Unit,
    onAssignJeu: () -> Unit,
    onUnassignJeu: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            // Ligne principale
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icône / numéro table
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("T${table.id}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Table #${table.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                    val info = buildString {
                        append("Capacité : ${table.nbJeuxActuels ?: 0}/${table.capaciteJeux} jeux")
                    }
                    Text(info, fontSize = 11.sp, color = TextMuted)
                    table.statut?.let {
                        Spacer(Modifier.height(2.dp))
                        StatutTableBadge(it)
                    }
                }

                // Bouton expand jeux
                IconButton(
                    onClick = {
                        if (!expanded) onExpand()
                        expanded = !expanded
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Masquer jeux" else "Voir jeux",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Retirer", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Section jeux de la table (expandable)
            if (expanded) {
                HorizontalDivider()
                if (jeux == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (jeux.isEmpty()) {
                            Text(
                                "Aucun jeu sur cette table",
                                fontSize = 11.sp, color = TextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            jeux.forEach { jeu ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (jeu.urlImage != null) {
                                            AsyncImage(
                                                model = jeu.urlImage,
                                                contentDescription = jeu.nom,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFDDE3EA)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("🎲", fontSize = 14.sp) }
                                        }
                                        Text(jeu.nom ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyBlue)
                                    }
                                    if (canRemove) {
                                        IconButton(onClick = { onUnassignJeu(jeu.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Désassigner", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                        if (canRemove && (table.nbJeuxActuels ?: 0) < table.capaciteJeux) {
                            TextButton(
                                onClick = onAssignJeu,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+ Assigner un jeu", fontSize = 11.sp, color = BrightBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatutTableBadge(statut: StatutTable) {
    val (bg, fg) = when (statut) {
        StatutTable.LIBRE -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        StatutTable.RESERVE -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        StatutTable.PLEIN -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        StatutTable.HORS_SERVICE -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(statut.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

// ─────────────────────────────────────────────────────
// Dialog ajout jeu
// ─────────────────────────────────────────────────────

@Composable
private fun AddJeuDialog(
    availableJeux: List<JeuDto>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedJeuId by remember { mutableStateOf<Int?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedJeu = availableJeux.find { it.id == selectedJeuId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un jeu", fontWeight = FontWeight.Bold) },
        text = {
            if (availableJeux.isEmpty()) {
                Text("Aucun jeu disponible pour cet éditeur.", color = TextMuted, fontSize = 13.sp)
            } else {
                Column {
                    Text("Jeu de l'éditeur :", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(selectedJeu?.nom ?: "Sélectionner un jeu", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableJeux.forEach { jeu ->
                                DropdownMenuItem(
                                    text = { Text(jeu.nom, fontSize = 13.sp) },
                                    onClick = { selectedJeuId = jeu.id; dropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selectedJeuId?.let(onConfirm) }, enabled = selectedJeuId != null) {
                Text("Ajouter")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ─────────────────────────────────────────────────────
// Composables locaux
// ─────────────────────────────────────────────────────

@Composable
private fun FlagBadge(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) Color(0xFFD1FAE5) else Color(0xFFF3F4F6))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (active) "✓ $label" else "✗ $label",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color(0xFF065F46) else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun JeuReservationCard(jeu: JeuFestivalViewDto, canRemove: Boolean = false, onRemove: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (jeu.urlImage != null) {
                AsyncImage(
                    model = jeu.urlImage,
                    contentDescription = jeu.jeuNom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFDDE3EA)),
                    contentAlignment = Alignment.Center
                ) { Text("🎲", fontSize = 20.sp) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(jeu.jeuNom ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                val meta = buildList {
                    if (jeu.typeJeuNom != null) add(jeu.typeJeuNom)
                    if (jeu.nbJoueursMin != null || jeu.nbJoueursMax != null)
                        add("${jeu.nbJoueursMin ?: "-"}–${jeu.nbJoueursMax ?: "-"} joueurs")
                    if (jeu.dureeMinutes != null) add("${jeu.dureeMinutes} min")
                    if (jeu.ageMin != null) add("${jeu.ageMin}+ ans")
                }.joinToString(" · ")
                if (meta.isNotEmpty()) Text(meta, fontSize = 10.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (jeu.dansListeObtenue) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFD1FAE5)).padding(horizontal = 5.dp, vertical = 2.dp)
                    ) { Text("Obtenu", fontSize = 8.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold) }
                }
                if (jeu.jeuxRecu) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFDBEAFE)).padding(horizontal = 5.dp, vertical = 2.dp)
                    ) { Text("Reçu", fontSize = 8.sp, color = BrightBlue, fontWeight = FontWeight.Bold) }
                }
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Retirer", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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
