package com.example.frontend.ui.screens.editeurs

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.repository.EditeurRepository
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ErrorBanner
import com.example.frontend.ui.theme.AppBackground
import com.example.frontend.ui.theme.BrightBlue
import com.example.frontend.ui.theme.NavyBlue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ────────────────────────────────────────────────────────────────

data class EditeurFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val nom: String = "",
    val logoUrl: String = ""
)

class EditeurFormViewModel(private val editeurId: Int? = null) : ViewModel() {
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)

    private val _uiState = MutableStateFlow(EditeurFormUiState(isEditMode = editeurId != null))
    val uiState = _uiState.asStateFlow()

    init {
        if (editeurId != null) loadEditeur(editeurId)
    }

    private fun loadEditeur(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val editeur = editeurRepository.getById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nom = editeur.nom,
                    logoUrl = editeur.logoUrl ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onNomChange(v: String) { _uiState.value = _uiState.value.copy(nom = v) }
    fun onLogoUrlChange(v: String) { _uiState.value = _uiState.value.copy(logoUrl = v) }

    fun save() {
        val s = _uiState.value
        if (s.nom.isBlank()) {
            _uiState.value = s.copy(error = "Le nom est obligatoire")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                if (editeurId != null) {
                    editeurRepository.update(editeurId, s.nom.trim(), s.logoUrl.ifBlank { null })
                } else {
                    editeurRepository.create(s.nom.trim(), s.logoUrl.ifBlank { null })
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun EditeurFormScreen(
    editeurId: Int? = null,
    onBack: () -> Unit,
    viewModel: EditeurFormViewModel = viewModel(
        factory = viewModelFactory { initializer { EditeurFormViewModel(editeurId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = if (uiState.isEditMode) "Modifier l'éditeur" else "Nouvel éditeur",
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
                    OutlinedTextField(
                        value = uiState.nom,
                        onValueChange = viewModel::onNomChange,
                        label = { Text("Nom *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                        )
                    )
                    OutlinedTextField(
                        value = uiState.logoUrl,
                        onValueChange = viewModel::onLogoUrlChange,
                        label = { Text("URL du logo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                        )
                    )
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
                        if (uiState.isEditMode) "Enregistrer" else "Créer l'éditeur",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
