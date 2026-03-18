package com.example.frontend.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FestivalFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val nom: String = "",
    val lieu: String = "",
    val dateDebut: String = "",
    val dateFin: String = "",
    val isEditMode: Boolean = false
)

class FestivalFormViewModel(private val festivalId: Int? = null) : ViewModel() {
    private val festivalRepository = FestivalRepository(RetrofitInstance.festivalApi)

    private val _uiState = MutableStateFlow(FestivalFormUiState(isEditMode = festivalId != null))
    val uiState = _uiState.asStateFlow()

    init {
        if (festivalId != null) loadFestival(festivalId)
    }

    private fun loadFestival(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val f = festivalRepository.getById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nom = f.nom,
                    lieu = f.lieu,
                    dateDebut = f.dateDebut,
                    dateFin = f.dateFin
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onNomChange(v: String) { _uiState.value = _uiState.value.copy(nom = v) }
    fun onLieuChange(v: String) { _uiState.value = _uiState.value.copy(lieu = v) }
    fun onDateDebutChange(v: String) { _uiState.value = _uiState.value.copy(dateDebut = v) }
    fun onDateFinChange(v: String) { _uiState.value = _uiState.value.copy(dateFin = v) }

    fun save() {
        val s = _uiState.value
        if (s.nom.isBlank() || s.lieu.isBlank() || s.dateDebut.isBlank() || s.dateFin.isBlank()) {
            _uiState.value = s.copy(error = "Tous les champs sont obligatoires")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                if (festivalId != null) {
                    festivalRepository.update(festivalId, s.nom.trim(), s.lieu.trim(), s.dateDebut.trim(), s.dateFin.trim())
                } else {
                    festivalRepository.create(s.nom.trim(), s.lieu.trim(), s.dateDebut.trim(), s.dateFin.trim())
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
