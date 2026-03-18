package com.example.frontend.ui.screens.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frontend.ui.theme.AppBackground
import com.example.frontend.ui.theme.NavyBlue
import com.example.frontend.ui.theme.BrightBlue


@Composable
fun FestivalFormScreen(
    festivalId: Int? = null,
    onBack: () -> Unit,
    viewModel: FestivalFormViewModel = viewModel(
        factory = viewModelFactory { initializer { FestivalFormViewModel(festivalId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    FestivalFormContent(
        nom = uiState.nom,
        lieu = uiState.lieu,
        dateDebut = uiState.dateDebut,
        dateFin = uiState.dateFin,
        isSaving = uiState.isSaving,
        isEditMode = uiState.isEditMode,
        onNomChange = viewModel::onNomChange,
        onLieuChange = viewModel::onLieuChange,
        onDateDebutChange = viewModel::onDateDebutChange,
        onDateFinChange = viewModel::onDateFinChange,
        onSave = viewModel::save
    )
}

@Composable
private fun FestivalFormContent(
    nom: String,
    lieu: String,
    dateDebut: String,
    dateFin: String,
    isSaving: Boolean,
    isEditMode: Boolean,
    onNomChange: (String) -> Unit,
    onLieuChange: (String) -> Unit,
    onDateDebutChange: (String) -> Unit,
    onDateFinChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FestivalTextField("Nom du festival *", nom, onNomChange)
                    FestivalTextField("Lieu *", lieu, onLieuChange)
                    FestivalTextField("Date de début (AAAA-MM-JJ) *", dateDebut, onDateDebutChange,
                        placeholder = "2025-03-01")
                    FestivalTextField("Date de fin (AAAA-MM-JJ) *", dateFin, onDateFinChange,
                        placeholder = "2025-03-03")
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (isEditMode) "Enregistrer les modifications" else "Créer le festival",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Formulaire - Créer un festival")
@Composable
private fun FestivalFormCreatePreview() {
    FestivalFormContent(
        nom = "",
        lieu = "",
        dateDebut = "",
        dateFin = "",
        isSaving = false,
        isEditMode = false,
        onNomChange = {},
        onLieuChange = {},
        onDateDebutChange = {},
        onDateFinChange = {},
        onSave = {}
    )
}

@Preview(showBackground = true, name = "Formulaire - Modifier un festival")
@Composable
private fun FestivalFormEditPreview() {
    FestivalFormContent(
        nom = "Festival à Jouer 2025",
        lieu = "Liège, Belgique",
        dateDebut = "2025-03-01",
        dateFin = "2025-03-03",
        isSaving = false,
        isEditMode = true,
        onNomChange = {},
        onLieuChange = {},
        onDateDebutChange = {},
        onDateFinChange = {},
        onSave = {}
    )
}

@Composable
private fun FestivalTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightBlue,
            focusedLabelColor = BrightBlue
        )
    )
}
