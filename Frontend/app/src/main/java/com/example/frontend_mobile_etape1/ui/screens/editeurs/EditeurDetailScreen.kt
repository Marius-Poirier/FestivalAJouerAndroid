package com.example.frontend_mobile_etape1.ui.screens.editeurs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.dto.PersonneDto
import com.example.frontend_mobile_etape1.ui.components.*
import com.example.frontend_mobile_etape1.ui.theme.*

@Composable
fun EditeurDetailScreen(
    editeurId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onJeuClick: (Int) -> Unit,
    viewModel: EditeurDetailViewModel = viewModel(
        key = "editeur_$editeurId",
        factory = viewModelFactory { initializer { EditeurDetailViewModel(editeurId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManage = viewModel.authManager.isAdminSuperorga
    val canSeeContacts = viewModel.authManager.isAdminSuperorgaOrga
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            itemName = uiState.editeur?.nom ?: "",
            onConfirm = { viewModel.delete { onBack() } },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = uiState.editeur?.nom ?: "Éditeur",
            showBackButton = true,
            onBackClick = onBack,
            actions = {
                if (canManage && uiState.editeur != null) {
                    IconButton(onClick = { uiState.editeur?.id?.let(onEdit) }) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                }
            }
        )

        if (uiState.isLoading) { LoadingOverlay(); return@Column }
        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(16.dp))
            return@Column
        }

        val editeur = uiState.editeur ?: return@Column

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header éditeur
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editeur.logoUrl != null) {
                            AsyncImage(
                                model = editeur.logoUrl,
                                contentDescription = editeur.nom,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            EditeurAvatar(editeur.nom, size = 60)
                        }
                        Column {
                            Text(editeur.nom, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyBlue)
                            Text("${uiState.jeux.size} jeu(x)", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            // Contacts (si droit)
            if (canSeeContacts && uiState.personnes.isNotEmpty()) {
                item {
                    SectionHeader("Contacts", uiState.personnes.size)
                }
                items(uiState.personnes, key = { "p_${it.id}" }) { personne ->
                    PersonneCard(
                        personne = personne,
                        canManage = canManage,
                        onDelete = { personne.id?.let { id -> viewModel.removePersonne(id) } }
                    )
                }
            }

            // Jeux
            if (uiState.jeux.isNotEmpty()) {
                item {
                    SectionHeader("Jeux", uiState.jeux.size)
                }
                items(uiState.jeux, key = { "j_${it.id}" }) { jeu ->
                    JeuMiniCard(jeu = jeu, onClick = { jeu.id?.let(onJeuClick) })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
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

@Composable
private fun PersonneCard(personne: PersonneDto, canManage: Boolean, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE8F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${personne.prenom} ${personne.nom}",
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue
                )
                if (personne.fonction != null) Text(personne.fonction, fontSize = 10.sp, color = TextMuted)
                Text(personne.telephone, fontSize = 10.sp, color = TextMuted)
                if (personne.email != null) Text(personne.email, fontSize = 10.sp, color = BrightBlue)
            }
            if (canManage) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun JeuMiniCard(jeu: JeuDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (jeu.urlImage != null) {
                AsyncImage(
                    model = jeu.urlImage,
                    contentDescription = jeu.nom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDDE3EA)),
                    contentAlignment = Alignment.Center
                ) { Text("🎲", fontSize = 18.sp) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(jeu.nom, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                val players = when {
                    jeu.nbJoueursMin != null && jeu.nbJoueursMax != null -> "👥 ${jeu.nbJoueursMin}–${jeu.nbJoueursMax}"
                    else -> ""
                }
                val duration = jeu.dureeMinutes?.let { "⏱ ~$it min" } ?: ""
                val info = listOf(players, duration).filter { it.isNotEmpty() }.joinToString(" · ")
                if (info.isNotEmpty()) Text(info, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}
