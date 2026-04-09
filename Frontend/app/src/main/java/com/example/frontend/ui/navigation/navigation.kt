package com.example.frontend.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomNavBar
import com.example.frontend.ui.components.LocalIsOffline
import com.example.frontend.ui.components.LocalOnAdminClick
import com.example.frontend.ui.components.LocalOnLogoClick
import com.example.frontend.ui.screens.auth.LoginScreen
import com.example.frontend.ui.screens.auth.RegisterScreen
import com.example.frontend.ui.screens.festivals.FestivalFormScreen
import com.example.frontend.ui.screens.festivals.FestivalListScreen
import com.example.frontend.ui.screens.festivals.FestivalListViewModel
import com.example.frontend.ui.screens.workflow.WorkflowViewModel
import com.example.frontend.ui.screens.festivals.FestivalFormScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.screens.jeux.JeuDetailScreen
import com.example.frontend.ui.screens.jeux.JeuFormScreen
import com.example.frontend.ui.screens.jeux.JeuListScreen

import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.FestivalApp
import com.example.frontend.data.dto.UserMeResponse
import com.example.frontend.ui.theme.BrightBlue
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import com.example.frontend.ui.screens.editeurs.EditeurDetailScreen
import com.example.frontend.ui.screens.editeurs.EditeurFormScreen
import com.example.frontend.ui.screens.editeurs.EditeurListScreen
import com.example.frontend.ui.screens.admin.AdminScreen
import com.example.frontend.ui.screens.workflow.ReservationFormScreen
import com.example.frontend.ui.screens.workflow.WorkflowScreen
import com.example.frontend.ui.utils.isNetworkAvailable


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

    // ── Vérification de session au démarrage ──────────────
    var isCheckingSession by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val dataStore = RetrofitInstance.userPreferencesDataStore
    val authManager = RetrofitInstance.authManager

    //verification si offline
    val isOffline = !isNetworkAvailable(LocalContext.current)

    // ── Navigation ───────────────────────────────────────


    val backStack = remember { mutableStateListOf<Any>(Login) }

    // Nécessaire pour vérifier l'état réseau dans LaunchedEffect
    val context = LocalContext.current

    val festivalListViewModel: FestivalListViewModel = viewModel()

    var jeuListReloadKey by remember { mutableIntStateOf(0) }
    var jeuDetailReloadKey by remember { mutableIntStateOf(0) }

    val currentDestination = backStack.lastOrNull()
    val showBottomNav = currentDestination?.let {
        it::class in bottomNavDestinations
    } ?: false

    LaunchedEffect(Unit) {
        scope.launch {
            dataStore.userPreferences.collect { prefs ->
                if (prefs.isLoggedIn) {
                    // 1. Restaure les cookies depuis le disque
                    val savedCookies = dataStore.getSavedCookies()
                    val host = "api.mxrjup.fun"
                    val cookies = savedCookies.map { (name, value, expiresAt) ->
                        okhttp3.Cookie.Builder()
                            .name(name)
                            .value(value)
                            .domain(host)
                            .expiresAt(expiresAt)
                            .build()
                    }
                    RetrofitInstance.cookieJar.restoreCookies(host, cookies)

                    if (isNetworkAvailable(context)) {
                        // ── En ligne : vérifier la session auprès du backend ──
                        val ok = authManager.whoami()
                        if (ok) {
                            android.util.Log.d("SESSION", "Session restaurée : ${prefs.email} / ${prefs.role}")
                            backStack.add(Home)
                        } else {
                            // Cookie réellement expiré → nettoyer et redemander le login
                            android.util.Log.d("SESSION", "Cookie expiré → Login")
                            dataStore.clearUser()
                            backStack.add(Login)
                        }
                    } else {
                        // ── Hors-ligne
                        // On NE appelle PAS whoami() — cela évite de détruire la session.
                        android.util.Log.d("SESSION", "Hors-ligne, session conservée pour ${prefs.email}")

                        // Reconstruire un UserMeResponse depuis les prefs stockées,
                        // pour que les contrôles de rôle (isAdminSuperorga, etc.) fonctionnent.
                        authManager.setUser(
                            UserMeResponse(
                                id = 0,
                                email = prefs.email,
                                role = prefs.role,
                                statut = prefs.statut,
                                dateDemande = null,
                                emailBloque = null,
                                createdAt = null
                            )
                        )

                        // Si des données sont en cache → aller directement au Workflow
                        val offlineRepo = (context.applicationContext as FestivalApp).offlineRepository
                        if (offlineRepo.hasCachedData()) {
                            android.util.Log.d("SESSION", "Données en cache → Workflow")
                            backStack.clear()
                            backStack.add(Workflow)
                        } else {
                            // Connecté mais pas de cache → Home (WorkflowViewModel gérera l'état offline)
                            android.util.Log.d("SESSION", "Pas de cache → Home")
                            backStack.add(Home)
                        }
                    }
                } else {
                    android.util.Log.d("SESSION", "Aucune session → Login")
                    backStack.add(Login)
                }
                isCheckingSession = false
                return@collect
            }
        }
    }
    // ── Écran de chargement pendant la vérification ───────
    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrightBlue)
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentDestination = currentDestination,
                    isOffline = isOffline,
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
            LocalIsOffline provides isOffline,
            LocalOnLogoClick provides {
                backStack.clear()
                backStack.add(Home)
            },
            LocalOnAdminClick provides {
                if (currentDestination?.let { it::class != Admin::class } != false) {
                    backStack.add(Admin)
                }
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
                            onEdit = { id -> backStack.add(JeuForm(jeuId = id)) },
                            reloadKey = jeuDetailReloadKey
                        )
                    }

                    entry<JeuForm> { dest ->
                        JeuFormScreen(
                            jeuId = if (dest.jeuId == 0) null else dest.jeuId,
                            editeurId = dest.editeurId,
                            onBack = { backStack.removeLastOrNull() },
                            onSaved = {
                                jeuListReloadKey++
                                jeuDetailReloadKey++
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
                            onBack = { backStack.removeLastOrNull() },
                            onJeuClick = { id -> backStack.add(JeuForm(jeuId = id)) },
                            onAddJeu = { backStack.add(JeuForm(editeurId = dest.editeurId)) }
                        )
                    }

                    // ── Workflow ──────────────────────────────────
                    entry<Workflow> {
                        val workflowViewModel: WorkflowViewModel = viewModel()
                        WorkflowScreen(
                            onEditReservation = { resaId, festivalId ->
                                backStack.add(ReservationForm(reservationId = resaId, festivalId = festivalId))
                            },
                            onCreateReservation = { festivalId ->
                                backStack.add(ReservationForm(festivalId = festivalId))
                            },
                            viewModel = workflowViewModel
                        )
                    }

                    entry<ReservationForm> { dest ->
                        // On récupère le ViewModel existant s'il existe (celui du workflow parent)
                        val workflowViewModel: WorkflowViewModel = viewModel()
                        ReservationFormScreen(
                            reservationId = if (dest.reservationId == 0) null else dest.reservationId,
                            festivalId = dest.festivalId,
                            onBack = {
                                workflowViewModel.loadReservations()
                                backStack.removeLastOrNull()
                            }
                        )
                    }

                    // ── Admin ─────────────────────────────────────
                    entry<Admin> {
                        AdminScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                }
            )
        }
    }
}
