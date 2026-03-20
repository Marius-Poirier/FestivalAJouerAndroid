package com.example.frontend_mobile_etape1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow

@Composable
fun StatutWorkflowBadge(
    statut: StatutWorkflow?,
    modifier: Modifier = Modifier
) {
    if (statut == null) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(statut.badgeBackground)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = statut.label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = statut.badgeText,
            letterSpacing = 0.5.sp
        )
    }
}
