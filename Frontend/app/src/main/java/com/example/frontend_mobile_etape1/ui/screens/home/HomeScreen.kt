package com.example.frontend_mobile_etape1.ui.screens.home

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
import com.example.frontend_mobile_etape1.ui.components.AppTopBar
import com.example.frontend_mobile_etape1.ui.components.InfoCard
import com.example.frontend_mobile_etape1.ui.utils.formatDateFr
import com.example.frontend_mobile_etape1.ui.theme.AppBackground
import com.example.frontend_mobile_etape1.ui.theme.BrightBlue
import com.example.frontend_mobile_etape1.ui.theme.NavyBlue

@Composable
fun HomeScreen(
    onGoToFestivals: () -> Unit,
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
                        onClick = onGoToFestivals,
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
                        Text(
                            festival.nom,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyBlue
                        )
                        InfoCard("📍", "LOCALISATION", festival.lieu)
                        InfoCard("📅", "DÉBUT", formatDateFr(festival.dateDebut))
                        InfoCard("🏁", "FIN", formatDateFr(festival.dateFin))
                    } else {
                        Text(
                            "Aucun festival disponible",
                            color = Color(0xFF888888),
                            fontSize = 13.sp
                        )
                    }
                }

                // Bienvenue
                if (user != null) {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Column {
                                Text(
                                    user!!.email,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyBlue
                                )
                                Text(
                                    user!!.role.uppercase(),
                                    fontSize = 10.sp,
                                    color = BrightBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}
