package com.example.frontend.ui.screens.editeurs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.auth.AuthManager
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.EditeurDto
import com.example.frontend.data.dto.JeuDto
import com.example.frontend.data.dto.PersonneDto
import com.example.frontend.data.repository.EditeurRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditeurDetailUiState(
    val isLoading: Boolean = false,
    val editeur: EditeurDto? = null,
    val jeux: List<JeuDto> = emptyList(),
    val personnes: List<PersonneDto> = emptyList(),
    val error: String? = null
)

class EditeurDetailViewModel(private val editeurId: Int) : ViewModel() {
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(EditeurDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val editeur = editeurRepository.getById(editeurId)
                val jeux = editeurRepository.getJeux(editeurId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, editeur = editeur, jeux = jeux
                )
                if (authManager.isAdminSuperorgaOrga) {
                    try {
                        val personnes = editeurRepository.getPersonnes(editeurId)
                        _uiState.value = _uiState.value.copy(personnes = personnes)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun removePersonne(personneId: Int) {
        viewModelScope.launch {
            try {
                editeurRepository.removePersonne(editeurId, personneId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression du contact")
            }
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                editeurRepository.delete(editeurId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
