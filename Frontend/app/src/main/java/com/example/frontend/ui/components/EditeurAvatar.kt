package com.example.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.BrightBlue

@Composable
fun EditeurAvatar(nom: String, size: Int = 32) {
    val initials = nom.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(BrightBlue),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = (size * 0.38f).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
