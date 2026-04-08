package com.example.frontend.ui.screens.jeux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
    editeurId: Int? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit = onBack,
    viewModel: JeuFormViewModel = viewModel(
        key = "jeuForm_${jeuId ?: 0}_${editeurId ?: 0}",
        factory = viewModelFactory { initializer { JeuFormViewModel(jeuId, editeurId) } }
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

                    // Éditeur(s)
                    if (uiState.editeurs.isNotEmpty()) {
                        var showEditeurDialog by remember { mutableStateOf(false) }
                        val selectedNames = uiState.editeurs
                            .filter { it.id != null && it.id in uiState.selectedEditeursIds }
                            .joinToString(", ") { it.nom }

                        OutlinedTextField(
                            value = selectedNames.ifEmpty { "Aucun éditeur sélectionné" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Éditeur(s)") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null,
                                    modifier = Modifier.clickable { showEditeurDialog = true })
                            },
                            modifier = Modifier.fillMaxWidth().clickable { showEditeurDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightBlue,
                                focusedLabelColor = BrightBlue
                            )
                        )

                        if (showEditeurDialog) {
                            var searchQuery by remember { mutableStateOf("") }
                            val filteredEditeurs = uiState.editeurs.filter {
                                it.nom.contains(searchQuery, ignoreCase = true)
                            }
                            AlertDialog(
                                onDismissRequest = { showEditeurDialog = false },
                                title = { Text("Sélectionner les éditeurs") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Rechercher un éditeur...") },
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightBlue,
                                                focusedLabelColor = BrightBlue
                                            )
                                        )
                                        androidx.compose.foundation.lazy.LazyColumn(
                                            modifier = Modifier.heightIn(max = 300.dp)
                                        ) {
                                            items(filteredEditeurs) { editeur ->
                                                val selected = editeur.id != null && editeur.id in uiState.selectedEditeursIds
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { editeur.id?.let { viewModel.onEditeurToggle(it) } }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selected,
                                                        onCheckedChange = { editeur.id?.let { viewModel.onEditeurToggle(it) } }
                                                    )
                                                    Text(editeur.nom, fontSize = 14.sp, color = NavyBlue)
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showEditeurDialog = false }) {
                                        Text("Fermer", color = BrightBlue)
                                    }
                                }
                            )
                        }
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
