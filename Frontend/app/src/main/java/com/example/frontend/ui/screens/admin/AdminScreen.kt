package com.example.frontend.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.enums.RoleUtilisateur
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ErrorBanner
import com.example.frontend.ui.components.LoadingOverlay
import com.example.frontend.ui.theme.*
import kotlinx.coroutines.delay

// ── Couleurs spécifiques au panel admin ──────────────
private val StatusPending = Color(0xFFF59E0B)
private val StatusPendingBg = Color(0xFFFEF3C7)
private val StatusActive = Color(0xFF10B981)
private val StatusActiveBg = Color(0xFFD1FAE5)
private val StatusBanned = Color(0xFFEF4444)
private val StatusBannedBg = Color(0xFFFEE2E2)
private val RoleBadgeBg = Color(0xFFEDE9FE)
private val RoleBadgeText = Color(0xFF6D28D9)

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Snackbar pour les succès d'action
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearActionSuccess()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground)
        ) {
            AppTopBar(
                title = "Administration",
                showBackButton = true,
                onBackClick = onBack
            )

            // En-tête page
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Gestion des utilisateurs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NavyBlue
                    )
                    Text(
                        "${uiState.users.size} utilisateur(s)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Compteurs rapides
            if (!uiState.isLoading && uiState.users.isNotEmpty()) {
                val pending = uiState.users.count {
                    it.statut.equals("en_attente", ignoreCase = true)
                }
                val banned = uiState.users.count {
                    it.statut.equals("refuse", ignoreCase = true) || it.email_bloque
                }
                val active = uiState.users.count {
                    it.statut.equals("valide", ignoreCase = true) && !it.email_bloque
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        label = "En attente",
                        count = pending,
                        bgColor = StatusPendingBg,
                        textColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "Actifs",
                        count = active,
                        bgColor = StatusActiveBg,
                        textColor = StatusActive,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "Bannis",
                        count = banned,
                        bgColor = StatusBannedBg,
                        textColor = StatusBanned,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Erreur
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                uiState.error?.let {
                    ErrorBanner(it, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // Contenu
            if (uiState.isLoading) {
                LoadingOverlay()
            } else if (uiState.users.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucun utilisateur trouvé", fontSize = 14.sp, color = TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.users, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            onAccept = { viewModel.acceptUser(user.id) },
                            onBan = { viewModel.banUser(user.id) },
                            onUnban = { viewModel.unbanUser(user.id) },
                            onChangeRole = { role -> viewModel.changeRole(user.id, role) }
                        )
                    }
                }
            }
        }
    }
}

// ── Carte utilisateur ────────────────────────────────
@Composable
private fun UserCard(
    user: AdminUserDto,
    onAccept: () -> Unit,
    onBan: () -> Unit,
    onUnban: () -> Unit,
    onChangeRole: (RoleUtilisateur) -> Unit
) {
    val isPending = user.statut.equals("en_attente", ignoreCase = true)
    val isBanned = user.statut.equals("refuse", ignoreCase = true) || user.email_bloque
    val isActive = user.statut.equals("valide", ignoreCase = true) && !user.email_bloque

    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // ── Ligne 1 : Avatar + Infos + Badge statut ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar cercle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isBanned -> StatusBannedBg
                                isPending -> StatusPendingBg
                                else -> Color(0xFFDBEAFE)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = when {
                            isBanned -> StatusBanned
                            isPending -> StatusPending
                            else -> BrightBlue
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.email,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "ID: ${user.id}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Badge statut
                StatusBadge(statut = user.statut, email_bloque = user.email_bloque)
            }

            Spacer(Modifier.height(10.dp))

            // ── Ligne 2 : Badge rôle ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleBadge(role = user.role)

                if (user.created_at != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardSecondary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "📅 ${formatAdminDate(user.created_at)}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Ligne 3 : Boutons d'action ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Accepter (seulement si en attente)
                if (isPending) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusActive,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Accepter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bannir / Débannir
                if (isBanned) {
                    OutlinedButton(
                        onClick = onUnban,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusActive
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Débannir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onBan,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Destructive
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Bannir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Changer rôle
                Box {
                    OutlinedButton(
                        onClick = { showRoleMenu = true },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RoleBadgeText
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Rôle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        RoleUtilisateur.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        roleDisplayName(role),
                                        fontSize = 13.sp,
                                        fontWeight = if (user.role.equals(role.name, ignoreCase = true))
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (user.role.equals(role.name, ignoreCase = true))
                                            BrightBlue else NavyBlue
                                    )
                                },
                                onClick = {
                                    showRoleMenu = false
                                    onChangeRole(role)
                                },
                                leadingIcon = {
                                    if (user.role.equals(role.name, ignoreCase = true)) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BrightBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Composants utilitaires ───────────────────────────

@Composable
private fun StatusBadge(statut: String?, email_bloque: Boolean) {
    val (bg, text, label) = when {
        statut.equals("en_attente", ignoreCase = true) ->
            Triple(StatusPendingBg, StatusPending, "En attente")
        statut.equals("refuse", ignoreCase = true) || email_bloque ->
            Triple(StatusBannedBg, StatusBanned, "Banni")
        statut.equals("valide", ignoreCase = true) && !email_bloque ->
            Triple(StatusActiveBg, StatusActive, "Actif")
        else ->
            Triple(CardSecondary, TextMuted, statut ?: "Inconnu")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = text)
    }
}

@Composable
private fun RoleBadge(role: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(RoleBadgeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            roleDisplayName(RoleUtilisateur.fromString(role)),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = RoleBadgeText
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

private fun roleDisplayName(role: RoleUtilisateur?): String = when (role) {
    RoleUtilisateur.ADMIN -> "Admin"
    RoleUtilisateur.SUPER_ORGANISATEUR -> "Super Organisateur"
    RoleUtilisateur.ORGANISATEUR -> "Organisateur"
    RoleUtilisateur.BENEVOLE -> "Bénévole"
    null -> "Inconnu"
}

private fun formatAdminDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val cleaned = iso.substringBefore('T')
        val parts = cleaned.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } catch (e: Exception) {
        iso ?: "—"
    }
}
