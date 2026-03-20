package com.example.frontend_mobile_etape1.ui.screens.editeurs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend_mobile_etape1.data.dto.EditeurDto
import com.example.frontend_mobile_etape1.ui.components.*
import com.example.frontend_mobile_etape1.ui.theme.*

@Composable
fun EditeurListScreen(
    onEditeurClick: (Int) -> Unit,
    onAddEditeur: () -> Unit,
    viewModel: EditeurListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val canManage = viewModel.authManager.isAdminSuperorga
    var editeurToDelete by remember { mutableStateOf<EditeurDto?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    if (editeurToDelete != null) {
        ConfirmDeleteDialog(
            itemName = editeurToDelete!!.nom,
            onConfirm = {
                editeurToDelete!!.id?.let { viewModel.delete(it) }
                editeurToDelete = null
            },
            onDismiss = { editeurToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Éditeurs")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Éditeurs", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                Text("${uiState.editeurs.size} éditeur(s)", fontSize = 10.sp, color = TextMuted)
            }
            if (canManage) FabAdd(onClick = onAddEditeur)
        }

        SearchBar(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = "Rechercher un éditeur...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(10.dp))

        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        } else if (uiState.editeurs.isEmpty()) {
            EmptyState("📚", "Aucun éditeur trouvé")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.editeurs, key = { it.id ?: it.nom }) { editeur ->
                    EditeurListItem(
                        editeur = editeur,
                        canManage = canManage,
                        onClick = { editeur.id?.let(onEditeurClick) },
                        onDelete = { editeurToDelete = editeur }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditeurListItem(
    editeur: EditeurDto,
    canManage: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (editeur.logoUrl != null) {
                AsyncImage(
                    model = editeur.logoUrl,
                    contentDescription = editeur.nom,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                EditeurAvatar(editeur.nom)
            }
            Text(
                editeur.nom,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NavyBlue,
                modifier = Modifier.weight(1f)
            )
            if (canManage) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Destructive, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
