package com.example.frontend.ui.screens.jeux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import coil.compose.AsyncImage
import com.example.frontend.data.dto.JeuDto
import com.example.frontend.ui.components.*
import com.example.frontend.ui.theme.*

@Composable
fun JeuListScreen(
    onJeuClick: (Int) -> Unit,
    onAddJeu: () -> Unit,
    onEditJeu: (Int) -> Unit,
    reloadKey: Int = 0,
    viewModel: JeuListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reloadKey) {
        if (reloadKey > 0) viewModel.load()
    }
    val canManage = viewModel.authManager.isAdminSuperorgaOrga
    var jeuToDelete by remember { mutableStateOf<JeuDto?>(null) }
    var searchText by remember { mutableStateOf("") }

    if (jeuToDelete != null) {
        ConfirmDeleteDialog(
            itemName = jeuToDelete!!.nom,
            onConfirm = {
                jeuToDelete!!.id?.let { viewModel.delete(it) }
                jeuToDelete = null
            },
            onDismiss = { jeuToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Jeux")

        // En-tête
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Jeux", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                Text("${uiState.filteredJeux.size} jeu(x)", fontSize = 10.sp, color = TextMuted)
            }
            if (canManage) {
                IconButton(onClick = onAddJeu) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un jeu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Recherche
        SearchBar(
            value = searchText,
            onValueChange = { searchText = it; viewModel.onSearchChange(it) },
            placeholder = "Rechercher un jeu...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(10.dp))

        // Filtres types
        if (uiState.typesJeu.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedTypeId == null,
                    onClick = { viewModel.onTypeFilter(null) },
                    label = { Text("Tous", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrightBlue,
                        selectedLabelColor = Color.White
                    )
                )
                uiState.typesJeu.forEach { type ->
                    FilterChip(
                        selected = uiState.selectedTypeId == type.id,
                        onClick = { viewModel.onTypeFilter(type.id) },
                        label = { Text(type.nom, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrightBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        } else if (uiState.filteredJeux.isEmpty()) {
            EmptyState("🎲", "Aucun jeu trouvé")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredJeux, key = { it.id ?: it.nom }) { jeu ->
                    JeuCard(
                        jeu = jeu,
                        canManage = canManage,
                        onClick = { jeu.id?.let(onJeuClick) },
                        onEdit = { jeu.id?.let { onEditJeu(it) } },
                        onDelete = { jeuToDelete = jeu }
                    )
                }
            }
        }
    }
}

@Composable
fun JeuCard(
    jeu: JeuDto,
    canManage: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                if (jeu.urlImage != null) {
                    AsyncImage(
                        model = jeu.urlImage,
                        contentDescription = jeu.nom,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFDDE3EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼️", fontSize = 20.sp)
                            Text("Pas d'image", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }

                // Menu 3 points en haut à droite
                if (canManage) {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable { menuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modifier", fontSize = 13.sp) },
                                onClick = { menuExpanded = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer", fontSize = 13.sp, color = Color(0xFFD4183D)) },
                                onClick = { menuExpanded = false; onDelete() }
                            )
                        }
                    }
                }
            }

            // Infos
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    jeu.nom,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = BrightBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                Spacer(Modifier.height(2.dp))
                val players = when {
                    jeu.nbJoueursMin != null && jeu.nbJoueursMax != null -> "👥 ${jeu.nbJoueursMin}–${jeu.nbJoueursMax}"
                    jeu.nbJoueursMin != null -> "👥 ${jeu.nbJoueursMin}+"
                    else -> ""
                }
                val duration = jeu.dureeMinutes?.let { "⏱ ~${it} min" } ?: ""
                val info = listOf(players, duration).filter { it.isNotEmpty() }.joinToString(" · ")
                if (info.isNotEmpty()) {
                    Text(info, fontSize = 9.sp, color = Color(0xFF555555))
                }
            }
        }
    }
}
