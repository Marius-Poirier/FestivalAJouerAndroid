package com.example.frontend.ui.composants

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.frontend.ui.theme.Destructive

@Composable
fun messageConfirmation(itemName: String, onConfirm: () -> Unit,onDismiss: () -> Unit) {
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