package com.example.frontend_mobile_etape1.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.UserMeResponse
import com.example.frontend_mobile_etape1.ui.components.AppTopBar
import com.example.frontend_mobile_etape1.ui.components.ErrorBanner
import com.example.frontend_mobile_etape1.ui.components.LoadingOverlay
import com.example.frontend_mobile_etape1.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ────────────────────────────────────────────────────────────────

data class AdminUiState(
    val isLoading: Boolean = false,
    val pendingUsers: List<UserMeResponse> = emptyList(),
    val allUsers: List<UserMeResponse> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class AdminViewModel : ViewModel() {
    private val userApi = RetrofitInstance.userApi

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val pending = userApi.getPendingUsers()
                val all = userApi.getUsers()
                _uiState.value = _uiState.value.copy(
                    isLoading = false, pendingUsers = pending, allUsers = all
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun validate(id: Int, role: String = "benevole") {
        viewModelScope.launch {
            try {
                userApi.validateUser(id, mapOf("role" to role))
                _uiState.value = _uiState.value.copy(successMessage = "Compte validé")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reject(id: Int) {
        viewModelScope.launch {
            try {
                userApi.rejectUser(id)
                _uiState.value = _uiState.value.copy(successMessage = "Compte refusé")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(title = "Administration", showBackButton = true, onBackClick = onBack)

        if (uiState.isLoading) { LoadingOverlay(); return@Column }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.error != null) {
                item { ErrorBanner(uiState.error!!) }
            }
            if (uiState.successMessage != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                    ) {
                        Text(
                            "✅ ${uiState.successMessage}",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF065F46), fontSize = 12.sp
                        )
                    }
                }
            }

            // Comptes en attente
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Comptes en attente",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue
                    )
                    Spacer(Modifier.width(8.dp))
                    if (uiState.pendingUsers.isNotEmpty()) {
                        Badge(containerColor = Destructive) {
                            Text("${uiState.pendingUsers.size}", color = Color.White)
                        }
                    }
                }
            }

            if (uiState.pendingUsers.isEmpty()) {
                item { Text("Aucun compte en attente", fontSize = 13.sp, color = TextMuted) }
            } else {
                items(uiState.pendingUsers, key = { it.id }) { user ->
                    PendingUserCard(
                        user = user,
                        onValidate = { viewModel.validate(user.id) },
                        onReject = { viewModel.reject(user.id) }
                    )
                }
            }

            // Tous les utilisateurs
            item {
                Spacer(Modifier.height(4.dp))
                Text("Tous les utilisateurs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
            }
            items(uiState.allUsers, key = { "all_${it.id}" }) { user ->
                UserCard(user)
            }
        }
    }
}

@Composable
private fun PendingUserCard(
    user: UserMeResponse,
    onValidate: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(user.email, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
            Text("En attente depuis : ${user.dateDemande ?: "—"}", fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onValidate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Valider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Destructive),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refuser", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: UserMeResponse) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.email, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                Text(user.role.uppercase(), fontSize = 10.sp, color = BrightBlue, fontWeight = FontWeight.Bold)
            }
            val statut = user.statut ?: "—"
            val statColor = when (statut) {
                "valide" -> Color(0xFF10B981)
                "en_attente" -> Color(0xFFF59E0B)
                "refuse" -> Destructive
                else -> TextMuted
            }
            Text(statut.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statColor)
        }
    }
}
