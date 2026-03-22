package com.example.frontend.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.navigation.BottomNavDestination
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.TextMuted

@Composable
fun BottomNavBar(
    currentDestination: Any?,
    onTabSelected: (Any) -> Unit
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
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(destination.route) },
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
