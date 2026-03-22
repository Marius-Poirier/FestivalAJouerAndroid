package com.example.frontend.ui.screens.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.data.dto.FestivalDto
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ErrorBanner
import com.example.frontend.ui.components.ConfirmDeleteDialog
import com.example.frontend.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FestivalListScreen(
    onFestivalClick: (Int) -> Unit,
    onAddFestival: () -> Unit,
    onEditFestival: (Int) -> Unit,
    viewModel: FestivalListViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.authManager.currentUser.collectAsStateWithLifecycle()
    val canManage = currentUser != null && viewModel.authManager.isAdminSuperorga
    var festivalToDelete by remember { mutableStateOf<FestivalDto?>(null) }

    if (festivalToDelete != null) {
        ConfirmDeleteDialog(
            itemName = festivalToDelete!!.nom,
            onConfirm = {
                festivalToDelete!!.id?.let { viewModel.delete(it) }
                festivalToDelete = null
            },
            onDismiss = { festivalToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Festivals")

        // En-tête page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Festivals", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                Text("${uiState.festivals.size} festival(s)", fontSize = 10.sp, color = TextMuted)
            }
            if (canManage) {
                FloatingActionButton(
                    onClick = onAddFestival,
                    modifier = Modifier.size(36.dp),
                    containerColor = BrightBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (uiState.error != null) {
            ErrorBanner(uiState.error!!, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrightBlue)
            }
        } else if (uiState.festivals.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎪", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Aucun festival trouvé", fontSize = 14.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.festivals, key = { it.id ?: it.nom }) { festival ->
                    FestivalCard(
                        festival = festival,
                        canManage = canManage,
                        onClick = { festival.id?.let(onFestivalClick) },
                        onEdit = { festival.id?.let(onEditFestival) },
                        onDelete = { festivalToDelete = festival }
                    )
                }
            }
        }
    }
}

@Composable
private fun FestivalCard(
    festival: FestivalDto,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        festival.nom,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyBlue
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(festival.lieu, fontSize = 11.sp, color = TextMuted)
                }
                if (canManage) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier",
                                tint = BrightBlue, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer",
                                tint = Destructive, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("📅 ${formatDate(festival.dateDebut)}", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyBlue)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🏁 ${formatDate(festival.dateFin)}", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyBlue)
                }
            }
        }
    }
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val cleaned = iso.substringBefore('T')
        val date = LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        iso
    }
}
