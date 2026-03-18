package com.example.frontend.ui.screens.festivals

import android.R.attr.button
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.data.dto.FestivalDto
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManage = viewModel.authManager.isAdminSuperorga
    var festivalToDelete by remember { mutableStateOf<FestivalDto?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
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
                Text("+")
            }
        }

        if (uiState.isLoading) {
            Text("Chargement")
        } else if (uiState.festivals.isEmpty()) {
            Text("🎪 Aucun festival trouvé")
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
                    Text("📅 ${formatDateFr(festival.dateDebut)}", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyBlue)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🏁 ${formatDateFr(festival.dateFin)}", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyBlue)
                }
            }
        }
    }
}

private val sampleFestival = FestivalDto(
    id = 1,
    nom = "Festival à Jouer 2025",
    lieu = "Liège, Belgique",
    dateDebut = "2025-03-01",
    dateFin = "2025-03-03"
)

@Preview(showBackground = true, name = "Festival Card - Administrateur")
@Composable
private fun FestivalCardAdminPreview() {
    FestivalCard(
        festival = sampleFestival,
        canManage = true,
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}

@Preview(showBackground = true, name = "Festival Card - Visiteur")
@Composable
private fun FestivalCardVisitorPreview() {
    FestivalCard(
        festival = sampleFestival,
        canManage = false,
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}

fun formatDateFr(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val cleaned = iso.substringBefore('T')
        val date = LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        iso
    }
}
