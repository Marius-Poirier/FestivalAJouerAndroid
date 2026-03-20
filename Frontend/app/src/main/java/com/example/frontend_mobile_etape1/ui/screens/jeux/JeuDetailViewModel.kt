package com.example.frontend_mobile_etape1.ui.screens.jeux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.repository.JeuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JeuDetailUiState(
    val isLoading: Boolean = false,
    val jeu: JeuDto? = null,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

class JeuDetailViewModel(private val jeuId: Int) : ViewModel() {
    private val jeuRepository = JeuRepository(RetrofitInstance.jeuApi, RetrofitInstance.metadataApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(JeuDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val jeu = jeuRepository.getById(jeuId)
                _uiState.value = _uiState.value.copy(isLoading = false, jeu = jeu)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                jeuRepository.delete(jeuId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
