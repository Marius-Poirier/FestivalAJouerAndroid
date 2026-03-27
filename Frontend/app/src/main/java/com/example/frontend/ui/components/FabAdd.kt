package com.example.frontend.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.BrightBlue

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
