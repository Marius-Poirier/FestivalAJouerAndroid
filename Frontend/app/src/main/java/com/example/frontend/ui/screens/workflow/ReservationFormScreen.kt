package com.example.frontend.ui.screens.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.*
import com.example.frontend.data.repository.EditeurRepository
import com.example.frontend.data.repository.WorkflowRepository
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ErrorBanner
import com.example.frontend.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

data class ReservationFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,

    val editeurs: List<EditeurDto> = emptyList(),
    val selectedEditeurId: Int? = null,
    val statutWorkflow: StatutWorkflow? = null,
    val editeurPresenteJeux: Boolean = false,
    val paiementRelance: Boolean = false,
    val remisePourcentage: String = "",
    val commentairesPaiement: String = "",
    val dateFacture: String = "",
    val datePaiement: String = ""
)

class ReservationFormViewModel(
    private val reservationId: Int?,
    private val festivalId: Int
) : ViewModel() {
    private val repo = WorkflowRepository(RetrofitInstance.workflowApi)
    private val editeurRepo = EditeurRepository(RetrofitInstance.editeurApi)

    private val _uiState = MutableStateFlow(ReservationFormUiState(isEditMode = reservationId != null))
    val uiState = _uiState.asStateFlow()

    private fun update(block: ReservationFormUiState.() -> ReservationFormUiState) {
        _uiState.value = _uiState.value.block()
    }

    init {
        loadEditeurs()
    }

    private fun loadEditeurs() {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            try {
                val editeurs = editeurRepo.getAll()
                if (reservationId == null) {
                    update { 
                        ReservationFormUiState(
                            editeurs = editeurs,
                            isLoading = false,
                            isEditMode = false
                        )
                    }
                } else {
                    update { copy(editeurs = editeurs, isLoading = false) }
                    loadReservation(reservationId)
                }
            } catch (e: Exception) {
                update { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadReservation(id: Int) {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            try {
                val reservations = repo.getReservations(festivalId)
                val resa = reservations.firstOrNull { it.id == id }
                if (resa != null) {
                    update {
                        copy(
                            isLoading = false,
                            selectedEditeurId = resa.editeurId,
                            statutWorkflow = resa.statutWorkflow,
                            editeurPresenteJeux = resa.editeurPresenteJeux,
                            paiementRelance = resa.paiementRelance,
                            remisePourcentage = resa.remisePourcentage?.toString() ?: "",
                            commentairesPaiement = resa.commentairesPaiement ?: "",
                            dateFacture = resa.dateFacture ?: "",
                            datePaiement = resa.datePaiement ?: ""
                        )
                    }
                } else {
                    update { copy(isLoading = false) }
                }
            } catch (e: Exception) {
                update { copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEditeurChange(id: Int) = update { copy(selectedEditeurId = id) }
    fun onStatutChange(s: StatutWorkflow?) = update { copy(statutWorkflow = s) }
    fun onEditeurPresenteJeuxChange(v: Boolean) = update { copy(editeurPresenteJeux = v) }
    fun onPaiementRelanceChange(v: Boolean) = update { copy(paiementRelance = v) }
    fun onRemisePourcentageChange(v: String) = update { copy(remisePourcentage = v) }
    fun onCommentairesChange(v: String) = update { copy(commentairesPaiement = v) }
    fun onDateFactureChange(v: String) = update { copy(dateFacture = v) }
    fun onDatePaiementChange(v: String) = update { copy(datePaiement = v) }

    fun onNavigated() = update { copy(isSuccess = false) }

    fun save() {
        val s = _uiState.value
        val editeurId = s.selectedEditeurId
        if (editeurId == null) {
            update { copy(error = "Veuillez sélectionner un éditeur") }
            return
        }
        viewModelScope.launch {
            update { copy(isSaving = true, error = null) }
            try {
                val req = CreateReservationRequest(
                    editeurId = editeurId,
                    festivalId = festivalId,
                    statutWorkflow = s.statutWorkflow,
                    editeurPresenteJeux = s.editeurPresenteJeux,
                    paiementRelance = s.paiementRelance,
                    remisePourcentage = s.remisePourcentage.toDoubleOrNull(),
                    commentairesPaiement = s.commentairesPaiement.ifBlank { null },
                    dateFacture = s.dateFacture.ifBlank { null },
                    datePaiement = s.datePaiement.ifBlank { null }
                )
                if (reservationId != null) repo.updateReservation(reservationId, req)
                else repo.createReservation(req)
                update { copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                update { copy(isSaving = false, error = e.message) }
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ReservationFormScreen(
    reservationId: Int?,
    festivalId: Int,
    onBack: () -> Unit,
    viewModel: ReservationFormViewModel = viewModel(
        // En création : clé unique par instance de composable → ViewModel toujours frais
        // En édition : clé stable → ViewModel réutilisé pour garder les données chargées
        key = if (reservationId != null) {
            "resa_form_${reservationId}_$festivalId"
        } else {
            remember { "resa_form_new_${System.nanoTime()}" }
        },
        factory = viewModelFactory {
            initializer { ReservationFormViewModel(reservationId, festivalId) }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onNavigated()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = if (uiState.isEditMode) "Modifier la réservation" else "Nouvelle réservation",
            showBackButton = true,
            onBackClick = onBack
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrightBlue)
            }
            return@Column
        }

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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Éditeur ───────────────────────────────────
                    FormSection("Éditeur") {
                        DropdownField(
                            label = "Éditeur *",
                            value = uiState.editeurs.firstOrNull { it.id == uiState.selectedEditeurId }?.nom
                                ?: "Sélectionner un éditeur",
                            items = uiState.editeurs,
                            itemLabel = { it.nom },
                            onSelect = { viewModel.onEditeurChange(it.id!!) },
                            enabled = !uiState.isEditMode
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    // ── Statut workflow ───────────────────────────
                    FormSection("Statut") {
                        DropdownField(
                            label = "Statut workflow",
                            value = uiState.statutWorkflow?.label ?: "Aucun",
                            items = listOf(null) + StatutWorkflow.entries,
                            itemLabel = { it?.label ?: "Aucun" },
                            onSelect = { viewModel.onStatutChange(it) }
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    // ── Options ───────────────────────────────────
                    FormSection("Options") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Éditeur présente ses jeux", fontSize = 13.sp, color = NavyBlue)
                            Switch(
                                checked = uiState.editeurPresenteJeux,
                                onCheckedChange = viewModel::onEditeurPresenteJeuxChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrightBlue)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Relance paiement", fontSize = 13.sp, color = NavyBlue)
                            Switch(
                                checked = uiState.paiementRelance,
                                onCheckedChange = viewModel::onPaiementRelanceChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrightBlue)
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // ── Financier ─────────────────────────────────
                    FormSection("Financier") {
                        OutlinedTextField(
                            value = uiState.remisePourcentage,
                            onValueChange = viewModel::onRemisePourcentageChange,
                            label = { Text("Remise (%)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                            )
                        )
                        OutlinedTextField(
                            value = uiState.commentairesPaiement,
                            onValueChange = viewModel::onCommentairesChange,
                            label = { Text("Commentaires paiement") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                            )
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    // ── Dates ─────────────────────────────────────
                    FormSection("Dates") {
                        OutlinedTextField(
                            value = uiState.dateFacture,
                            onValueChange = viewModel::onDateFactureChange,
                            label = { Text("Date facture (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                            )
                        )
                        OutlinedTextField(
                            value = uiState.datePaiement,
                            onValueChange = viewModel::onDatePaiementChange,
                            label = { Text("Date paiement (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
                            )
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
                        if (uiState.isEditMode) "Enregistrer" else "Créer la réservation",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Composants locaux ─────────────────────────────────────────────────────────

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted)
        content()
    }
}

@Composable
private fun <T> DropdownField(
    label: String,
    value: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown, null,
                    modifier = Modifier.clickable(enabled = enabled) { expanded = true }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item), fontSize = 13.sp) },
                    onClick = { onSelect(item); expanded = false }
                )
            }
        }
    }
}
