package com.example.frontend.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomNavBar
import com.example.frontend.ui.components.LocalOnLogoClick
import com.example.frontend.ui.screens.auth.LoginScreen
import com.example.frontend.ui.screens.auth.RegisterScreen
import com.example.frontend.ui.screens.festivals.FestivalListScreen
import com.example.frontend.ui.screens.festivals.FestivalListViewModel
import com.example.frontend.ui.screens.festivals.FestivalFormScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.screens.jeux.JeuDetailScreen
import com.example.frontend.ui.screens.jeux.JeuFormScreen
import com.example.frontend.ui.screens.jeux.JeuListScreen
import com.example.frontend.ui.screens.editeurs.EditeurDetailScreen
import com.example.frontend.ui.screens.editeurs.EditeurFormScreen
import com.example.frontend.ui.screens.editeurs.EditeurListScreen
import com.example.frontend.ui.screens.workflow.ReservationFormScreen
import com.example.frontend.ui.screens.workflow.WorkflowScreen

// Destinations qui affichent la BottomNavBar
private val bottomNavDestinations = setOf(
    Home :: class,
    Festivals::class,
    JeuList::class,
    EditeurList::class,
    Workflow::class
)

@Composable
fun AppNavGraph() {

    val backStack = remember { mutableStateListOf<Any>(Login) }

    val festivalListViewModel: FestivalListViewModel = viewModel()

    var jeuListReloadKey by remember { mutableIntStateOf(0) }

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
                        // Évite les doublons dans la backStack
                        if (currentDestination?.let { it::class != destination::class } == true) {
                            backStack.add(destination)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOnLogoClick provides {
                backStack.clear()
                backStack.add(Home)
            }
        ) {
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
                            onGoToApp = {
                                backStack.add(Festivals)
                            }
                        )
                    }

                    // ── Festivals ─────────────────────────────────
                    entry<Festivals> {
                        FestivalListScreen(
                            onFestivalClick = { },
                            onAddFestival = { backStack.add(FestivalForm()) },
                            onEditFestival = { id -> backStack.add(FestivalForm(festivalId = id)) },
                            viewModel = festivalListViewModel
                        )
                    }

                    entry<FestivalForm> { dest ->
                        FestivalFormScreen(
                            festivalId = dest.festivalId,
                            onBack = {
                                backStack.removeLastOrNull()
                                festivalListViewModel.load()
                            }
                        )
                    }

                    // ── Jeux ──────────────────────────────────────
                    entry<JeuList> {
                        JeuListScreen(
                            onJeuClick = { id -> backStack.add(JeuDetail(jeuId = id)) },
                            onAddJeu = { backStack.add(JeuForm()) },
                            onEditJeu = { id -> backStack.add(JeuForm(jeuId = id)) },
                            reloadKey = jeuListReloadKey
                        )
                    }

                    entry<JeuDetail> { dest ->
                        JeuDetailScreen(
                            jeuId = dest.jeuId,
                            onBack = { backStack.removeLastOrNull() },
                            onEdit = { id -> backStack.add(JeuForm(jeuId = id)) }
                        )
                    }

                    entry<JeuForm> { dest ->
                        JeuFormScreen(
                            jeuId = if (dest.jeuId == 0) null else dest.jeuId,
                            onBack = { backStack.removeLastOrNull() },
                            onSaved = {
                                jeuListReloadKey++
                                backStack.removeLastOrNull()
                            }
                        )
                    }

                    // ── Éditeurs ──────────────────────────────────
                    entry<EditeurList> {
                        EditeurListScreen(
                            onEditeurClick = { id -> backStack.add(EditeurDetail(editeurId = id)) },
                            onAddEditeur = { backStack.add(EditeurForm()) }
                        )
                    }

                    entry<EditeurDetail> { dest ->
                        EditeurDetailScreen(
                            editeurId = dest.editeurId,
                            onBack = { backStack.removeLastOrNull() },
                            onEdit = { id -> backStack.add(EditeurForm(editeurId = id)) },
                            onJeuClick = { id -> backStack.add(JeuDetail(jeuId = id)) }
                        )
                    }

                    entry<EditeurForm> { dest ->
                        EditeurFormScreen(
                            editeurId = if (dest.editeurId == 0) null else dest.editeurId,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    // ── Workflow ──────────────────────────────────
                    entry<Workflow> {
                        WorkflowScreen(
                            onEditReservation = { resaId, festivalId ->
                                backStack.add(ReservationForm(reservationId = resaId, festivalId = festivalId))
                            },
                            onCreateReservation = { festivalId ->
                                backStack.add(ReservationForm(festivalId = festivalId))
                            }
                        )
                    }

                    entry<ReservationForm> { dest ->
                        ReservationFormScreen(
                            reservationId = if (dest.reservationId == 0) null else dest.reservationId,
                            festivalId = dest.festivalId,
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
}
