package com.example.frontend.ui.screens.jeux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ErrorBanner
import com.example.frontend.ui.components.TypeJeuSelector
import com.example.frontend.ui.theme.AppBackground
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuFormScreen(
    jeuId: Int? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit = onBack,
    viewModel: JeuFormViewModel = viewModel(
        key = "jeuForm_${jeuId ?: 0}",
        factory = viewModelFactory { initializer { JeuFormViewModel(jeuId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onNavigated()
            onSaved()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = if (uiState.isEditMode) "Modifier le jeu" else "Nouveau jeu",
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
            if (uiState.error != null) ErrorBanner(uiState.error!!)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionTitle("Informations générales")

                    JeuTextField("Nom *", uiState.nom, viewModel::onNomChange)

                    // Type de jeu
                    if (uiState.typesJeu.isNotEmpty()) {
                        Text("Type de jeu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        TypeJeuSelector(
                            types = uiState.typesJeu,
                            selectedId = uiState.selectedTypeId,
                            onSelected = viewModel::onTypeSelected
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        JeuTextField(
                            "Joueurs min", uiState.nbJoueursMin, viewModel::onNbMinChange,
                            KeyboardType.Number, Modifier.weight(1f)
                        )
                        JeuTextField(
                            "Joueurs max", uiState.nbJoueursMax, viewModel::onNbMaxChange,
                            KeyboardType.Number, Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        JeuTextField(
                            "Durée (min)", uiState.dureeMinutes, viewModel::onDureeChange,
                            KeyboardType.Number, Modifier.weight(1f)
                        )
                        JeuTextField(
                            "Âge min", uiState.ageMin, viewModel::onAgeMinChange,
                            KeyboardType.Number, Modifier.weight(1f)
                        )
                    }

                    JeuTextField("Thème", uiState.theme, viewModel::onThemeChange)
                    JeuTextField("URL image", uiState.urlImage, viewModel::onUrlImageChange)

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue,
                            focusedLabelColor = BrightBlue
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Prototype", fontSize = 13.sp, color = NavyBlue)
                        Switch(
                            checked = uiState.prototype,
                            onCheckedChange = viewModel::onPrototypeChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                                checkedTrackColor = BrightBlue)
                        )
                    }
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
                        if (uiState.isEditMode) "Enregistrer" else "Créer le jeu",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
    HorizontalDivider(color = Color(0xFFE8EDF2))
}

@Composable
private fun JeuTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightBlue,
            focusedLabelColor = BrightBlue
        )
    )
}
