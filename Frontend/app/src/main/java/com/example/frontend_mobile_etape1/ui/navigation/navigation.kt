package com.example.frontend_mobile_etape1.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend_mobile_etape1.ui.components.BottomNavBar
import com.example.frontend_mobile_etape1.ui.screens.auth.LoginScreen
import com.example.frontend_mobile_etape1.ui.screens.auth.RegisterScreen
import com.example.frontend_mobile_etape1.ui.screens.editeurs.EditeurDetailScreen
import com.example.frontend_mobile_etape1.ui.screens.editeurs.EditeurFormScreen
import com.example.frontend_mobile_etape1.ui.screens.editeurs.EditeurListScreen
import com.example.frontend_mobile_etape1.ui.screens.festivals.FestivalFormScreen
import com.example.frontend_mobile_etape1.ui.screens.festivals.FestivalListScreen
import com.example.frontend_mobile_etape1.ui.screens.home.HomeScreen
import com.example.frontend_mobile_etape1.ui.screens.jeux.JeuDetailScreen
import com.example.frontend_mobile_etape1.ui.screens.jeux.JeuFormScreen
import com.example.frontend_mobile_etape1.ui.screens.jeux.JeuListScreen
import com.example.frontend_mobile_etape1.ui.screens.reservation.ReservationDetailScreen
import com.example.frontend_mobile_etape1.ui.screens.reservation.ReservationFormScreen
import com.example.frontend_mobile_etape1.ui.screens.workflow.WorkflowScreen

// Destinations qui affichent la BottomNavBar
private val bottomNavDestinations = setOf(
    FestivalList::class,
    JeuList::class,
    EditeurList::class,
    Workflow::class
)

@Composable
fun AppNavGraph() {

    val backStack = remember { mutableStateListOf<Any>(Login) }
    val currentDestination = backStack.lastOrNull()
    val showBottomNav = currentDestination?.let {
        it::class in bottomNavDestinations
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onTabSelected = { destination ->
                        // Evite les doublons dans la backStack
                        if (currentDestination?.let { it::class != destination::class } == true) {
                            backStack.add(destination)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {

                // ── Auth ──────────────────────────────────────
                entry<Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Home)
                        },
                        onGoToRegister = {
                            backStack.add(Register)
                        }
                    )
                }

                entry<Register> {
                    RegisterScreen(
                        onRegisterSuccess = { backStack.removeLastOrNull() },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // ── Home ──────────────────────────────────────
                entry<Home> {
                    HomeScreen(
                        onGoToFestivals = { backStack.add(FestivalList) },
                        onGoToAdmin = { backStack.add(Admin) }
                    )
                }

                // ── Festivals ─────────────────────────────────
                entry<FestivalList> {
                    FestivalListScreen(
                        onFestivalClick = { _ -> backStack.add(Workflow) },
                        onAddFestival = { backStack.add(FestivalForm()) },
                        onEditFestival = { id -> backStack.add(FestivalForm(id)) }
                    )
                }

                entry<FestivalForm> {
                    FestivalFormScreen(
                        festivalId = it.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // ── Jeux ──────────────────────────────────────
                entry<JeuList> {
                    JeuListScreen(
                        onJeuClick = { id -> 
                            Log.d("Navigation", "Navigating to JeuDetail with id: $id")
                            backStack.add(JeuDetail(id)) 
                        },
                        onAddJeu = { backStack.add(JeuForm()) }
                    )
                }

                entry<JeuDetail> {
                    Log.d("Navigation", "Entering entry<JeuDetail> with id: ${it.id}")
                    JeuDetailScreen(
                        jeuId = it.id,
                        onBack = { backStack.removeLastOrNull() },
                        onEdit = { id -> backStack.add(JeuForm(id)) }
                    )
                }

                entry<JeuForm> {
                    JeuFormScreen(
                        jeuId = it.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // ── Éditeurs ──────────────────────────────────
                entry<EditeurList> {
                    EditeurListScreen(
                        onEditeurClick = { id -> 
                            Log.d("Navigation", "Navigating to EditeurDetail with id: $id")
                            backStack.add(EditeurDetail(id)) 
                        },
                        onAddEditeur = { backStack.add(EditeurForm()) }
                    )
                }

                entry<EditeurDetail> {
                    Log.d("Navigation", "Entering entry<EditeurDetail> with id: ${it.id}")
                    EditeurDetailScreen(
                        editeurId = it.id,
                        onBack = { backStack.removeLastOrNull() },
                        onEdit = { id -> backStack.add(EditeurForm(id)) },
                        onJeuClick = { id -> backStack.add(JeuDetail(id)) }
                    )
                }

                entry<EditeurForm> {
                    EditeurFormScreen(
                        editeurId = it.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // ── Workflow ──────────────────────────────────
                entry<Workflow> {
                    WorkflowScreen(
                        onReservationClick = { id -> backStack.add(ReservationDetail(id)) },
                        onAddReservation = { festivalId -> backStack.add(ReservationForm(festivalId = festivalId)) },
                        onEditeurClick = { id -> backStack.add(EditeurDetail(id)) },
                        onJeuClick = { id -> backStack.add(JeuDetail(id)) },
                        onEditReservation = { id -> backStack.add(ReservationForm(id)) }
                    )
                }

                // ── Réservations ──────────────────────────────
                entry<ReservationDetail> {
                    ReservationDetailScreen(
                        reservationId = it.id,
                        onBack = { backStack.removeLastOrNull() },
                        onEdit = { id -> backStack.add(ReservationForm(id)) }
                    )
                }

                entry<ReservationForm> {
                    ReservationFormScreen(
                        reservationId = it.id,
                        festivalId = it.festivalId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // ── Admin ─────────────────────────────────────
                entry<Admin> {
                    // À implémenter
                }
            }
        )
    }
}
