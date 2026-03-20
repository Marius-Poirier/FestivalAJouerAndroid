package com.example.frontend_mobile_etape1.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// ── Routes sans paramètres ────────────────────────
@Serializable data object Login
@Serializable data object Register
@Serializable data object Home
@Serializable data object FestivalList
@Serializable data object JeuList
@Serializable data object EditeurList
@Serializable data object Workflow
@Serializable data object Admin

@Serializable data class JeuDetail(val id: Int)
@Serializable data class JeuForm(val id: Int? = null)
@Serializable data class EditeurDetail(val id: Int)
@Serializable data class EditeurForm(val id: Int? = null)
@Serializable data class FestivalForm(val id: Int? = null)
@Serializable data class ReservationDetail(val id: Int)
@Serializable data class ReservationForm(val id: Int? = null, val festivalId: Int? = null)


enum class BottomNavDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    FESTIVALS(FestivalList, "Festivals", Icons.Default.Festival, "Festivals"),
    JEUX(JeuList, "Jeux", Icons.Default.Games, "Jeux"),
    EDITEURS(EditeurList, "Éditeurs", Icons.Default.Business, "Éditeurs"),
    WORKFLOW(Workflow, "Workflow", Icons.Default.AccountTree, "Workflow")
}