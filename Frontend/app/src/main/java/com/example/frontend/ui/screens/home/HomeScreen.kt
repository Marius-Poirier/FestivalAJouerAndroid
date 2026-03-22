package com.example.frontend.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import com.example.frontend.ui.components.AppTopBar
// import com.example.frontend.ui.components.InfoCard
import com.example.frontend.ui.theme.AppBackground
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.NavyBlue
import com.example.frontend.ui.theme.TextMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun HomeScreen(
    onGoToApp: () -> Unit,          // renommé depuis onGoToFestivals pour correspondre à navigation.kt
    onGoToAdmin: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModel.authManager.currentUser.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Accueil")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero gradient ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(NavyBlue, BrightBlue))
                    )
            ) {
                // Cercles décoratifs
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 280.dp, y = (-30).dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-20).dp, y = 120.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.05f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Festival à Jouer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Organisez, gérez et animez vos\nfestivals de jeux de société",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onGoToApp,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = NavyBlue
                        )
                    ) {
                        Text("Accéder aux festivals →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // ── Festival en cours ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "ÉVÉNEMENT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue,
                        letterSpacing = 0.8.sp
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        color = BrightBlue
                    )
                } else {
                    val festival = uiState.latestFestival
                    if (festival != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    festival.nom,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NavyBlue
                                )
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                // InfoCard("📍", "LOCALISATION", festival.lieu)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Localisation",
                                        tint = BrightBlue, modifier = Modifier.size(16.dp))
                                    Text(festival.lieu, fontSize = 13.sp, color = NavyBlue)
                                }
                                // InfoCard("📅", "DÉBUT", formatDateFr(festival.dateDebut))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Date de début",
                                        tint = BrightBlue, modifier = Modifier.size(16.dp))
                                    Text(formatDate(festival.dateDebut), fontSize = 13.sp, color = NavyBlue)
                                }
                                // InfoCard("🏁", "FIN", formatDateFr(festival.dateFin))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = "Date de fin",
                                        tint = BrightBlue, modifier = Modifier.size(16.dp))
                                    Text(formatDate(festival.dateFin), fontSize = 13.sp, color = NavyBlue)
                                }
                            }
                        }
                    } else {
                        Text(
                            "Aucun festival disponible",
                            color = Color(0xFF888888),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val cleaned = iso.substringBefore('T')
        val parsed = LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: DateTimeParseException) {
        iso
    }
}
