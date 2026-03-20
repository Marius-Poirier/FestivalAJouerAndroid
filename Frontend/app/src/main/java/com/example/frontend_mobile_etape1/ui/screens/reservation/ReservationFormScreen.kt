package com.example.frontend_mobile_etape1.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.CreateReservationRequest
import com.example.frontend_mobile_etape1.data.dto.UpdateReservationRequest
import com.example.frontend_mobile_etape1.data.dto.EditeurDto
import com.example.frontend_mobile_etape1.data.enums.StatutWorkflow
import com.example.frontend_mobile_etape1.data.repository.EditeurRepository
import com.example.frontend_mobile_etape1.data.repository.ReservationRepository
import com.example.frontend_mobile_etape1.ui.components.AppTopBar
import com.example.frontend_mobile_etape1.ui.components.ErrorBanner
import com.example.frontend_mobile_etape1.ui.theme.AppBackground
import com.example.frontend_mobile_etape1.ui.theme.BrightBlue
import com.example.frontend_mobile_etape1.ui.theme.NavyBlue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ────────────────────────────────────────────────────────────────

data class ReservationFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val editeurs: List<EditeurDto> = emptyList(),
    // Form fields
    val editeurId: Int? = null,
    val festivalId: Int? = null,
    val statutWorkflow: StatutWorkflow = StatutWorkflow.PAS_CONTACTE,
    val editeurPresenteJeux: Boolean = false,
    val prixTotal: String = "",
    val remisePourcentage: String = "",
    val prixFinal: String = "",
    val paiementRelance: Boolean = false,
    val commentairesPaiement: String = "",
    val dateFacture: String = "",
    val datePaiement: String = ""
)

class ReservationFormViewModel(
    private val reservationId: Int? = null,
    private val initialFestivalId: Int? = null
) : ViewModel() {
    private val reservationRepository = ReservationRepository(RetrofitInstance.reservationApi)
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)

    private val _uiState = MutableStateFlow(ReservationFormUiState(
        isEditMode = reservationId != null,
        festivalId = initialFestivalId
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadEditeurs()
        if (reservationId != null) loadReservation(reservationId)
    }

    private fun loadEditeurs() {
        viewModelScope.launch {
            try {
                val list = editeurRepository.getAll()
                _uiState.value = _uiState.value.copy(editeurs = list)
            } catch (_: Exception) {}
        }
    }

    private fun loadReservation(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val r = reservationRepository.getById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    editeurId = r.editeurId,
                    festivalId = r.festivalId,
                    statutWorkflow = r.statutWorkflow ?: StatutWorkflow.PAS_CONTACTE,
                    editeurPresenteJeux = r.editeurPresenteJeux,
                    prixTotal = r.prixTotal?.toString() ?: "",
                    remisePourcentage = r.remisePourcentage?.toString() ?: "",
                    prixFinal = r.prixFinal?.toString() ?: "",
                    paiementRelance = r.paiementRelance,
                    commentairesPaiement = r.commentairesPaiement ?: "",
                    dateFacture = r.dateFacture ?: "",
                    datePaiement = r.datePaiement ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Handlers
    fun onEditeurChange(id: Int) { _uiState.value = _uiState.value.copy(editeurId = id) }
    fun onStatutChange(s: StatutWorkflow) { _uiState.value = _uiState.value.copy(statutWorkflow = s) }
    fun onPresenteJeuxChange(b: Boolean) { _uiState.value = _uiState.value.copy(editeurPresenteJeux = b) }
    fun onPrixTotalChange(v: String) { _uiState.value = _uiState.value.copy(prixTotal = v) }
    fun onRemiseChange(v: String) { _uiState.value = _uiState.value.copy(remisePourcentage = v) }
    fun onPrixFinalChange(v: String) { _uiState.value = _uiState.value.copy(prixFinal = v) }
    fun onRelanceChange(b: Boolean) { _uiState.value = _uiState.value.copy(paiementRelance = b) }
    fun onCommentairesChange(v: String) { _uiState.value = _uiState.value.copy(commentairesPaiement = v) }
    fun onDateFactureChange(v: String) { _uiState.value = _uiState.value.copy(dateFacture = v) }
    fun onDatePaiementChange(v: String) { _uiState.value = _uiState.value.copy(datePaiement = v) }

    fun save() {
        val s = _uiState.value
        if (s.editeurId == null || s.festivalId == null) {
            _uiState.value = s.copy(error = "Éditeur et Festival obligatoires")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                if (reservationId != null) {
                    val updateReq = UpdateReservationRequest(
                        statutWorkflow = s.statutWorkflow.apiValue,
                        editeurPresenteJeux = s.editeurPresenteJeux,
                        remisePourcentage = s.remisePourcentage.toDoubleOrNull(),
                        remiseMontant = null,
                        commentairesPaiement = s.commentairesPaiement.ifBlank { null },
                        paiementRelance = s.paiementRelance,
                        dateFacture = s.dateFacture.ifBlank { null },
                        datePaiement = s.datePaiement.ifBlank { null }
                    )
                    val response = reservationRepository.update(reservationId, updateReq)
                    if (!response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "Erreur: ${response.code()}")
                        return@launch
                    }
                } else {
                    val createReq = CreateReservationRequest(
                        editeurId = s.editeurId,
                        festivalId = s.festivalId,
                        statutWorkflow = s.statutWorkflow.apiValue,
                        editeurPresenteJeux = s.editeurPresenteJeux,
                        paiementRelance = s.paiementRelance
                    )
                    val response = reservationRepository.create(createReq)
                    if (!response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "Erreur: ${response.code()}")
                        return@launch
                    }
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen(
    reservationId: Int? = null,
    festivalId: Int? = null,
    onBack: () -> Unit,
    viewModel: ReservationFormViewModel = viewModel(
        key = "res_form_${reservationId ?: "new_$festivalId"}",
        factory = viewModelFactory { initializer { ReservationFormViewModel(reservationId, festivalId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        AppTopBar(
            title = if (uiState.isEditMode) "Modifier réservation" else "Nouvelle réservation",
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
                    Text("Éditeur", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    if (!uiState.isEditMode) {
                        var expanded by remember { mutableStateOf(false) }
                        val selectedEditeur = uiState.editeurs.find { it.id == uiState.editeurId }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = selectedEditeur?.nom ?: "Sélectionner un éditeur",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                uiState.editeurs.forEach { editeur ->
                                    DropdownMenuItem(
                                        text = { Text(editeur.nom) },
                                        onClick = {
                                            editeur.id?.let { viewModel.onEditeurChange(it) }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        val editeur = uiState.editeurs.find { it.id == uiState.editeurId }
                        Text(editeur?.nom ?: "Chargement...", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color(0xFFE8EDF2))

                    Text("Statut", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    var expandedStatut by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandedStatut, onExpandedChange = { expandedStatut = it }) {
                        OutlinedTextField(
                            value = uiState.statutWorkflow.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatut) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedStatut, onDismissRequest = { expandedStatut = false }) {
                            StatutWorkflow.entries.forEach { statut ->
                                DropdownMenuItem(
                                    text = { Text(statut.label) },
                                    onClick = { viewModel.onStatutChange(statut); expandedStatut = false }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.editeurPresenteJeux, onCheckedChange = viewModel::onPresenteJeuxChange)
                        Text("L'éditeur présente ses jeux", fontSize = 13.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResaTextField("Prix Total", uiState.prixTotal, viewModel::onPrixTotalChange, KeyboardType.Decimal, Modifier.weight(1f))
                        ResaTextField("Remise (%)", uiState.remisePourcentage, viewModel::onRemiseChange, KeyboardType.Number, Modifier.weight(1f))
                    }
                    ResaTextField("Prix Final", uiState.prixFinal, viewModel::onPrixFinalChange, KeyboardType.Decimal)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.paiementRelance, onCheckedChange = viewModel::onRelanceChange)
                        Text("Paiement relancé", fontSize = 13.sp)
                    }

                    ResaTextField("Commentaires paiement", uiState.commentairesPaiement, viewModel::onCommentairesChange)
                    ResaTextField("Date Facture (AAAA-MM-JJ)", uiState.dateFacture, viewModel::onDateFactureChange)
                    ResaTextField("Date Paiement (AAAA-MM-JJ)", uiState.datePaiement, viewModel::onDatePaiementChange)
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
                    Text(if (uiState.isEditMode) "Enregistrer" else "Créer la réservation", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResaTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightBlue, focusedLabelColor = BrightBlue)
    )
}
