package com.example.frontend_mobile_etape1.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend_mobile_etape1.data.dto.TypeJeuDto
import com.example.frontend_mobile_etape1.ui.theme.BrightBlue

@Composable
fun TypeJeuSelector(
    types: List<TypeJeuDto>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedId == null,
            onClick = { onSelected(null) },
            label = { Text("Aucun", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrightBlue,
                selectedLabelColor = Color.White
            )
        )
        types.forEach { type ->
            FilterChip(
                selected = selectedId == type.id,
                onClick = { onSelected(if (selectedId == type.id) null else type.id) },
                label = { Text(type.nom, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrightBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
