package com.example.frontend_mobile_etape1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.frontend_mobile_etape1.ui.theme.*

// ────────────────────────────────────────────────
// Barre de recherche
// ────────────────────────────────────────────────

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            BasicTextField_compat(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BasicTextField_compat(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 11.sp,
            color = TextDark
        ),
        modifier = modifier,
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 11.sp, color = TextMuted)
            }
            innerTextField()
        }
    )
}

// ────────────────────────────────────────────────
// FAB Bouton +
// ────────────────────────────────────────────────

@Composable
fun FabAdd(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        containerColor = BrightBlue,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
    }
}

// ────────────────────────────────────────────────
// Chargement
// ────────────────────────────────────────────────

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrightBlue)
    }
}

// ────────────────────────────────────────────────
// État vide
// ────────────────────────────────────────────────

@Composable
fun EmptyState(emoji: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 14.sp, color = TextMuted)
    }
}

// ────────────────────────────────────────────────
// Bandeau d'erreur
// ────────────────────────────────────────────────

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEE2E2))
            .padding(12.dp)
    ) {
        Text(message, color = Destructive, fontSize = 12.sp)
    }
}

// ────────────────────────────────────────────────
// Dialog confirmation suppression
// ────────────────────────────────────────────────

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer", fontWeight = FontWeight.Bold) },
        text = { Text("Êtes-vous sûr de vouloir supprimer « $itemName » ?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = Destructive, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

// ────────────────────────────────────────────────
// Avatar éditeur (initiales sur fond coloré)
// ────────────────────────────────────────────────

@Composable
fun EditeurAvatar(nom: String, url: String? = null, modifier: Modifier = Modifier, size: Int = 40) {
    val initials = nom.split(" ", "-")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { nom.take(2).uppercase() }

    val bgColor = Color(0xFFE8F4F8)
    val textColor = Color(0xFF1A6B8A)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = nom,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.28f).sp,
                color = textColor
            )
        }
    }
}

// ────────────────────────────────────────────────
// Info card (localisation, dates, etc.)
// ────────────────────────────────────────────────

@Composable
fun InfoCard(emoji: String, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrightBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 16.sp)
            }
            Column {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            }
        }
    }
}

// ────────────────────────────────────────────────
// Bloc info (stat box compact)
// ────────────────────────────────────────────────

@Composable
fun StatBox(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardSecondary)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 12.sp)
            Text(label, fontSize = 8.sp, color = TextMuted)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
        }
    }
}
