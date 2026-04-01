package com.example.frontend.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.AdminUserDto
import com.example.frontend.data.enums.RoleUtilisateur
import com.example.frontend.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val users: List<AdminUserDto> = emptyList(),
    val error: String? = null,
    val actionSuccess: String? = null
)

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository(RetrofitInstance.adminApi)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = null)
            try {
                val list = repository.getAllUsers()
                _uiState.value = _uiState.value.copy(isLoading = false, users = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du chargement des utilisateurs"
                )
            }
        }
    }

    fun acceptUser(userId: Int) {
        viewModelScope.launch {
            try {
                // Par défaut on valide en tant que bénévole, 
                // on pourrait le rendre dynamique si l'UI le permettait.
                val response = repository.validateUser(userId, "benevole")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(actionSuccess = "Utilisateur accepté")
                    load()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors de l'acceptation")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun banUser(userId: Int) {
        viewModelScope.launch {
            try {
                val user = _uiState.value.users.find { it.id == userId }
                if (user == null) return@launch
                
                val response = if (user.statut == "en_attente") {
                    repository.rejectUser(userId)
                } else {
                    repository.blockUser(userId, true)
                }
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(actionSuccess = "Utilisateur banni/refusé")
                    load()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors du bannissement")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unbanUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.blockUser(userId, false)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(actionSuccess = "Utilisateur débanni")
                    load()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors du débannissement")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun changeRole(userId: Int, newRole: RoleUtilisateur) {
        // Feature non supportée par le backend actuellement pour les utilisateurs déjà validés.
        _uiState.value = _uiState.value.copy(error = "Modification de rôle non supportée par le serveur actuel")
    }

    fun clearActionSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
