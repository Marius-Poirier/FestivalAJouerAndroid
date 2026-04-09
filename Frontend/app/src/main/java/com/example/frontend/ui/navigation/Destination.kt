package com.example.frontend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Games
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// ── Routes sans paramètres ────────────────────────
@Serializable data object Login
@Serializable data object Register
@Serializable data object Home

@Serializable data object Festivals

@Serializable data object JeuList
@Serializable data object EditeurList
@Serializable data object Workflow
@Serializable data object Admin

// ── Routes avec paramètres ────────────────────────
@Serializable data class JeuDetail(val jeuId: Int)
@Serializable data class JeuForm(val jeuId: Int = 0, val editeurId: Int? = null) // 0 = création
@Serializable data class FestivalForm(val festivalId: Int? = null)
@Serializable data class EditeurDetail(val editeurId: Int)
@Serializable data class EditeurForm(val editeurId: Int = 0) // 0 = création
@Serializable data class ReservationForm(val reservationId: Int = 0, val festivalId: Int) // 0 = création

enum class BottomNavDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    FESTIVALS(Festivals, "Festivals", Icons.Default.Festival, "Festivals"),
    JEUX(JeuList, "Jeux", Icons.Default.Games, "Jeux"),
    EDITEURS(EditeurList, "Éditeurs", Icons.Default.Business, "Éditeurs"),
    WORKFLOW(Workflow, "Workflow", Icons.Default.AccountTree, "Workflow")
}



