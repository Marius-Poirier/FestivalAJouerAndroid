package com.example.frontend_mobile_etape1.ui.screens.editeurs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.EditeurDto
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.dto.PersonneDto
import com.example.frontend_mobile_etape1.data.repository.EditeurRepository
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

    init {
        Log.d("EditeurDetail", "Initializing ViewModel for editeurId: $editeurId")
        load()
    }

    fun load() {
        viewModelScope.launch {
            Log.d("EditeurDetail", "Loading data for editeurId: $editeurId")
            _uiState.value = _uiState.value.copy(isLoading = true, editeur = null, jeux = emptyList(), error = null)
            try {
                val editeur = editeurRepository.getById(editeurId)
                Log.d("EditeurDetail", "Fetched editeur: ${editeur.nom} (ID: ${editeur.id})")
                
                val jeux = editeurRepository.getJeux(editeurId)
                Log.d("EditeurDetail", "Fetched ${jeux.size} jeux for editeurId: $editeurId")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false, editeur = editeur, jeux = jeux
                )
                if (authManager.isAdminSuperorgaOrga) {
                    try {
                        val personnes = editeurRepository.getPersonnes(editeurId)
                        Log.d("EditeurDetail", "Fetched ${personnes.size} personnes for editeurId: $editeurId")
                        _uiState.value = _uiState.value.copy(personnes = personnes)
                    } catch (e: Exception) {
                        Log.e("EditeurDetail", "Error fetching personnes: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("EditeurDetail", "Error loading editeur detail: ${e.message}")
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
