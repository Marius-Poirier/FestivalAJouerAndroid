package com.example.frontend_mobile_etape1.ui.screens.jeux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.ui.components.*
import com.example.frontend_mobile_etape1.ui.theme.*

@Composable
fun JeuListScreen(
    onJeuClick: (Int) -> Unit,
    onAddJeu: () -> Unit,
    viewModel: JeuListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Jeux", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                Text("${uiState.filteredJeux.size} jeu(x)", fontSize = 10.sp, color = TextMuted)
            }
            if (canManage) FabAdd(onClick = onAddJeu)
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
                        onEdit = { /* handled via detail screen */ },
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
                // Boutons action superposés
                if (canManage) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable(onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete, null,
                                tint = Color.White, modifier = Modifier.size(12.dp)
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
