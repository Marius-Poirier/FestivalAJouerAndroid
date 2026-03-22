package com.example.frontend.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.ui.theme.NavyBlue

/** CompositionLocals permettant à AppTopBar d'afficher le bouton admin
 *  sans modifier la signature de chaque screen. Fournis dans AppNavGraph. */
val authManager = RetrofitInstance.authManager
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
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val isAdmin = currentUser != null && authManager.isAdmin

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 20.dp),
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (showBackButton) 16.sp else 17.sp,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(content = actions)
                if (isAdmin) {
                    IconButton(
                        onClick = LocalOnAdminClick.current,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Administration",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
