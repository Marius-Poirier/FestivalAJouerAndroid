package com.example.frontend_mobile_etape1.ui.screens.jeux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.CreateJeuRequest
import com.example.frontend_mobile_etape1.data.dto.MecanismeDto
import com.example.frontend_mobile_etape1.data.dto.TypeJeuDto
import com.example.frontend_mobile_etape1.data.repository.JeuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JeuFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val nom: String = "",
    val nbJoueursMin: String = "",
    val nbJoueursMax: String = "",
    val dureeMinutes: String = "",
    val ageMin: String = "",
    val description: String = "",
    val theme: String = "",
    val urlImage: String = "",
    val prototype: Boolean = false,
    val selectedTypeId: Int? = null,
    val selectedEditeursIds: List<Int> = emptyList(),
    val selectedMecanismesIds: List<Int> = emptyList(),
    val typesJeu: List<TypeJeuDto> = emptyList(),
    val mecanismes: List<MecanismeDto> = emptyList()
)

class JeuFormViewModel(private val jeuId: Int? = null) : ViewModel() {
    private val jeuRepository = JeuRepository(RetrofitInstance.jeuApi, RetrofitInstance.metadataApi)

    private val _uiState = MutableStateFlow(JeuFormUiState(isEditMode = jeuId != null))
    val uiState = _uiState.asStateFlow()

    init {
        loadMetadata()
        if (jeuId != null) loadJeu(jeuId)
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            try {
                val types = jeuRepository.getTypesJeu()
                val mecas = jeuRepository.getMecanismes()
                _uiState.value = _uiState.value.copy(typesJeu = types, mecanismes = mecas)
            } catch (_: Exception) {}
        }
    }

    private fun loadJeu(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val jeu = jeuRepository.getById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nom = jeu.nom,
                    nbJoueursMin = jeu.nbJoueursMin?.toString() ?: "",
                    nbJoueursMax = jeu.nbJoueursMax?.toString() ?: "",
                    dureeMinutes = jeu.dureeMinutes?.toString() ?: "",
                    ageMin = jeu.ageMin?.toString() ?: "",
                    description = jeu.description ?: "",
                    theme = jeu.theme ?: "",
                    urlImage = jeu.urlImage ?: "",
                    prototype = jeu.prototype ?: false,
                    selectedTypeId = jeu.typeJeuId,
                    selectedEditeursIds = jeu.editeurs?.map { it.id } ?: emptyList(),
                    selectedMecanismesIds = jeu.mecanismes?.map { it.id } ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onNomChange(v: String) { _uiState.value = _uiState.value.copy(nom = v) }
    fun onNbMinChange(v: String) { _uiState.value = _uiState.value.copy(nbJoueursMin = v) }
    fun onNbMaxChange(v: String) { _uiState.value = _uiState.value.copy(nbJoueursMax = v) }
    fun onDureeChange(v: String) { _uiState.value = _uiState.value.copy(dureeMinutes = v) }
    fun onAgeMinChange(v: String) { _uiState.value = _uiState.value.copy(ageMin = v) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onThemeChange(v: String) { _uiState.value = _uiState.value.copy(theme = v) }
    fun onUrlImageChange(v: String) { _uiState.value = _uiState.value.copy(urlImage = v) }
    fun onPrototypeChange(v: Boolean) { _uiState.value = _uiState.value.copy(prototype = v) }
    fun onTypeSelected(id: Int?) { _uiState.value = _uiState.value.copy(selectedTypeId = id) }

    fun save() {
        val s = _uiState.value
        if (s.nom.isBlank()) {
            _uiState.value = s.copy(error = "Le nom est obligatoire")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val request = CreateJeuRequest(
                    nom = s.nom.trim(),
                    nbJoueursMin = s.nbJoueursMin.toIntOrNull(),
                    nbJoueursMax = s.nbJoueursMax.toIntOrNull(),
                    dureeMinutes = s.dureeMinutes.toIntOrNull(),
                    ageMin = s.ageMin.toIntOrNull(),
                    ageMax = null,
                    description = s.description.ifBlank { null },
                    lienRegles = null,
                    theme = s.theme.ifBlank { null },
                    urlImage = s.urlImage.ifBlank { null },
                    prototype = s.prototype,
                    typeJeuId = s.selectedTypeId,
                    editeursIds = s.selectedEditeursIds,
                    mecanismesIds = s.selectedMecanismesIds
                )
                if (jeuId != null) {
                    jeuRepository.update(jeuId, request)
                } else {
                    jeuRepository.create(request)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
