package com.example.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.NavyBlue

/** CompositionLocals permettant à AppTopBar d'afficher le bouton admin
 *  sans modifier la signature de chaque screen. Fournis dans AppNavGraph. */
val LocalIsAdmin = staticCompositionLocalOf { false }
val LocalOnAdminClick = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * TopBar principale de l'app — fond bleu marine #1A3A5C.
 * Affiche le logo 🎲, le titre "Festival à Jouer" et optionnellement
 * un bouton retour ou une icône d'action.
 */
@Composable
fun AppTopBar(
    title: String = "Festival à Jouer",
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyBlue)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
            } else {
                // Logo + Titre
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4A9EFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎲", fontSize = 14.sp)
                    }
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (showBackButton) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(content = actions)
                // Bouton administration (ADMIN uniquement, injecté via CompositionLocal)
                if (LocalIsAdmin.current) {
                    IconButton(
                        onClick = LocalOnAdminClick.current,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("⚙️", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
