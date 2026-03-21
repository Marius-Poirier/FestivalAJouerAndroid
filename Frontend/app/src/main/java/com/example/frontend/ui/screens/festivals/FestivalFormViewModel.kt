package com.example.frontend.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
                    dateDebut = isoToDdMmYyyy(f.dateDebut),
                    dateFin = isoToDdMmYyyy(f.dateFin)
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

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            nom = "",
            lieu = "",
            dateDebut = "",
            dateFin = ""
        )
    }

    fun save() {
        val s = _uiState.value
        if (s.nom.isBlank() || s.lieu.isBlank() || s.dateDebut.isBlank() || s.dateFin.isBlank()) {
            _uiState.value = s.copy(error = "Tous les champs sont obligatoires")
            return
        }
        val isoDebut = ddMmYyyyToIso(s.dateDebut)
        val isoFin = ddMmYyyyToIso(s.dateFin)
        if (isoDebut == null || isoFin == null) {
            _uiState.value = s.copy(error = "Format de date invalide. Utilisez JJ-MM-AAAA (ex: 21-03-2026)")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val response = if (festivalId != null) {
                    festivalRepository.update(festivalId, s.nom.trim(), s.lieu.trim(), isoDebut, isoFin)
                } else {
                    festivalRepository.create(s.nom.trim(), s.lieu.trim(), isoDebut, isoFin)
                }
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = "Erreur serveur (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    // "21-03-2026" → "2026-03-21" conversion frontend backend
    private fun ddMmYyyyToIso(date: String): String? {
        return try {
            val cleaned = date.trim()
            val parsed = LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            parsed.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // "2026-03-21" → "21-03-2026" convertion backend frontend
    private fun isoToDdMmYyyy(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val cleaned = date.substringBefore('T')
            val parsed = LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
            parsed.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (e: DateTimeParseException) {
            date
        }
    }
}
