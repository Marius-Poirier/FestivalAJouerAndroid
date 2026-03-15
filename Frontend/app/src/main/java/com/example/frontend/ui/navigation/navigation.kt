package com.example.frontend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.collection.mutableObjectListOf
import androidx.compose.runtime.mutableStateListOf


import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend.ui.screens.auth.LoginScreen
import com.example.frontend.ui.screens.auth.RegisterScreen
import com.example.frontend.ui.screens.home.HomeScreen

@Composable
fun AppNavGraph() {

    val backStack = remember { mutableStateListOf<Any>(Login) }

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

            // ── Home (placeholder) ────────────────────────
            entry<Home> {
                HomeScreen()
            }
        }
    )
}