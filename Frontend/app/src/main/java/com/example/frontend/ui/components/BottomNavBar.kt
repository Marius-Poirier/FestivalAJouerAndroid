package com.example.frontend.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.navigation.BottomNavDestination
import com.example.frontend.ui.navigation.Workflow
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.TextMuted

@Composable
fun BottomNavBar(
    currentDestination: Any?,
    onTabSelected: (Any) -> Unit,
    isOffline: Boolean = false
) {
    NavigationBar(
        modifier = Modifier.height(95.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        BottomNavDestination.entries.forEach { destination ->
            val isSelected = currentDestination?.let {
                it::class == destination.route::class
            } ?: false
            val isWorkflow = destination.route::class == Workflow::class
            val enabled = !isOffline || isWorkflow

            NavigationBarItem(
                selected = isSelected,
                onClick = { if (enabled) onTabSelected(destination.route) },
                modifier = Modifier.alpha(if (enabled) 1f else 0.35f),
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.contentDescription
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) BrightBlue else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrightBlue,
                    unselectedIconColor = TextMuted,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
