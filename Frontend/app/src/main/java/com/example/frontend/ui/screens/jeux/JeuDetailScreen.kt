package com.example.frontend.ui.screens.jeux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.frontend.ui.components.*
import com.example.frontend.ui.theme.*

@Composable
fun JeuDetailScreen(
    jeuId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: JeuDetailViewModel = viewModel(
        key = "jeu_$jeuId",
        factory = viewModelFactory { initializer { JeuDetailViewModel(jeuId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManage = viewModel.authManager.isAdminSuperorgaOrga
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            itemName = uiState.jeu?.nom ?: "",
            onConfirm = { viewModel.delete { onBack() } },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = uiState.jeu?.nom ?: "Jeu",
            showBackButton = true,
            onBackClick = onBack,
            actions = {
                if (canManage && uiState.jeu != null) {
                    IconButton(onClick = { uiState.jeu?.id?.let(onEdit) }) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                }
            }
        )

        if (uiState.isLoading) {
            LoadingOverlay()
            return@Column
        }
        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(16.dp))
            return@Column
        }

        val jeu = uiState.jeu ?: return@Column

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Image hero
            if (jeu.urlImage != null) {
                AsyncImage(
                    model = jeu.urlImage,
                    contentDescription = jeu.nom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFDDE3EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎲", fontSize = 48.sp)
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Titre + badges
                Text(jeu.nom, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (jeu.typeJeuNom != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDBEAFE))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(jeu.typeJeuNom, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        }
                    }
                    if (jeu.prototype == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Prototype", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                }

                // Stats en grille
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val players = when {
                        jeu.nbJoueursMin != null && jeu.nbJoueursMax != null -> "${jeu.nbJoueursMin}–${jeu.nbJoueursMax}"
                        jeu.nbJoueursMin != null -> "${jeu.nbJoueursMin}+"
                        else -> "—"
                    }
                    StatBox("👥", "Joueurs", players, Modifier.weight(1f))
                    StatBox("⏱", "Durée", jeu.dureeMinutes?.let { "~$it min" } ?: "—", Modifier.weight(1f))
                    StatBox("🎂", "Âge", jeu.ageMin?.let { "$it+" } ?: "—", Modifier.weight(1f))
                }

                // Description
                if (!jeu.description.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Description", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                            Spacer(Modifier.height(4.dp))
                            Text(jeu.description, fontSize = 12.sp, color = Color(0xFF555555), lineHeight = 18.sp)
                        }
                    }
                }

                // Éditeurs
                if (!jeu.editeurs.isNullOrEmpty()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Éditeurs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                            Spacer(Modifier.height(6.dp))
                            jeu.editeurs.forEach { editeur ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    EditeurAvatar(editeur.nom, size = 28)
                                    Text(editeur.nom, fontSize = 12.sp, color = Color(0xFF333333))
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Mécanismes
                if (!jeu.mecanismes.isNullOrEmpty()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Mécanismes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                            Spacer(Modifier.height(6.dp))
                            jeu.mecanismes.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    row.forEach { meca ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEEF2F7))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(meca.nom, fontSize = 10.sp, color = NavyBlue)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
