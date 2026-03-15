package com.example.frontend.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.auth.AuthManager
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel() : ViewModel() {

    private val authRepository = AuthRepository(RetrofitInstance.authApi)
    private val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)


    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email et mot de passe requis")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authRepository.login(email.trim(), password)
                if (response.isSuccessful && response.body()?.user != null) {
                    // Récupère les infos complètes via /users/me (cookie posé par le login)
                    val ok = authManager.whoami()
                    if (ok) {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Impossible de récupérer les informations du compte"
                        )
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Identifiants invalides"
                        403 -> "Compte en attente de validation ou bloqué"
                        0 -> "Serveur injoignable"
                        else -> "Erreur serveur (${response.code()})"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Impossible de contacter le serveur : ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
