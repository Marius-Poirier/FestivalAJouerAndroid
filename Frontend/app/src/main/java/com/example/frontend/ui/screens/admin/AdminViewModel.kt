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
                val response = repository.updateUserStatus(userId, "valide")
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
                val response = repository.updateUserStatus(userId, "banni")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(actionSuccess = "Utilisateur banni")
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
                val response = repository.updateUserStatus(userId, "valide")
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
        viewModelScope.launch {
            try {
                val roleString = when (newRole) {
                    RoleUtilisateur.ADMIN -> "admin"
                    RoleUtilisateur.SUPER_ORGANISATEUR -> "super_organisateur"
                    RoleUtilisateur.ORGANISATEUR -> "organisateur"
                    RoleUtilisateur.BENEVOLE -> "benevole"
                }
                val response = repository.updateUserRole(userId, roleString)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(actionSuccess = "Rôle modifié")
                    load()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors du changement de rôle")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearActionSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
