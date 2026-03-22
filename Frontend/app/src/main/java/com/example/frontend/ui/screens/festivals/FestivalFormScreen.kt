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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.screens.festivals.FestivalFormViewModel
import com.example.frontend.ui.composants.ErrorMessage
import com.example.frontend.ui.theme.AppBackground
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.NavyBlue

@Composable
fun FestivalFormScreen(
    festivalId: Int? = null,
    onBack: () -> Unit,
    viewModel: FestivalFormViewModel = viewModel(
        key = "festival_form_${festivalId}",
        factory = viewModelFactory { initializer { FestivalFormViewModel(festivalId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = if (uiState.isEditMode) "Modifier le festival" else "Nouveau festival",
            showBackButton = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.error != null) ErrorMessage(uiState.error!!)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FestivalTextField("Nom du festival *", uiState.nom, viewModel::onNomChange)
                    FestivalTextField("Lieu *", uiState.lieu, viewModel::onLieuChange)
                    FestivalTextField("Date de début (JJ-MM-AAAA) *", uiState.dateDebut, viewModel::onDateDebutChange,
                        placeholder = "01-03-2025")
                    FestivalTextField("Date de fin (JJ-MM-AAAA) *", uiState.dateFin, viewModel::onDateFinChange,
                        placeholder = "03-03-2025")
                }
            }

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (uiState.isEditMode) "Enregistrer les modifications" else "Créer le festival",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
