package com.example.frontend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend.ui.screens.auth.LoginScreen
import com.example.frontend.ui.screens.auth.RegisterScreen
import com.example.frontend.ui.screens.festivals.FestivalListScreen
import com.example.frontend.ui.screens.festivals.FestivalListViewModel
import com.example.frontend.ui.screens.festivals.FestivalFormScreen
import com.example.frontend.ui.screens.home.HomeScreen

@Composable
fun AppNavGraph() {

    val backStack = remember { mutableStateListOf<Any>(Login) }
    val festivalListViewModel: FestivalListViewModel = viewModel()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
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
                    onRegisterSuccess = {
                        backStack.removeLastOrNull()
                    },
                    onBack = {
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
            }
        }
    )
}