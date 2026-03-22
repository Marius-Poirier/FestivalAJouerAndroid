package com.example.frontend.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomNavBar
import com.example.frontend.ui.screens.auth.LoginScreen
import com.example.frontend.ui.screens.auth.RegisterScreen
import com.example.frontend.ui.screens.festivals.FestivalListScreen
import com.example.frontend.ui.screens.festivals.FestivalListViewModel
import com.example.frontend.ui.screens.festivals.FestivalFormScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.screens.jeux.JeuDetailScreen
import com.example.frontend.ui.screens.jeux.JeuFormScreen
import com.example.frontend.ui.screens.jeux.JeuListScreen

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
                            backStack.add(FestivalList)
                        }
                    )
                }

                // ── Festivals ─────────────────────────────────
                entry<FestivalList> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTopBar(title = "Festivals")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("À venir")
                        }
                    }
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

            // ── Home ──────────────────────────────────────
            entry<Home> {
                HomeScreen(
                    onGoToFestivals = { backStack.add(Festivals) }
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
            entry<FestivalForm> {
                FestivalFormScreen(
                    festivalId = it.festivalId,
                    onBack = {
                        backStack.removeLastOrNull()
                        festivalListViewModel.load()
                    }
                )
                // ── Éditeurs ──────────────────────────────────
                entry<EditeurList> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTopBar(title = "Éditeurs")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("À venir")
                        }
                    }
                }

                // ── Workflow ──────────────────────────────────
                entry<Workflow> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTopBar(title = "Workflow")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("À venir")
                        }
                    }
                }

                // ── Admin ─────────────────────────────────────
                entry<Admin> {
                    // À implémenter
                }
            }
        )
    }
}
