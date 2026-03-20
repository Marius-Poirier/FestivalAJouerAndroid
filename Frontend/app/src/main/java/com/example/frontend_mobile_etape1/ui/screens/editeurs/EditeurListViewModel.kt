package com.example.frontend_mobile_etape1.ui.screens.editeurs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.EditeurDto
import com.example.frontend_mobile_etape1.data.repository.EditeurRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditeurListUiState(
    val isLoading: Boolean = false,
    val editeurs: List<EditeurDto> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
class EditeurListViewModel : ViewModel() {
    private val editeurRepository = EditeurRepository(RetrofitInstance.editeurApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(EditeurListUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query -> load(query) }
        }
    }

    fun onSearchChange(query: String) { _searchQuery.value = query }

    fun load(search: String = _searchQuery.value) {
        viewModelScope.launch {
            Log.d("EditeurList", "Loading editeurs with search: '$search'")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = editeurRepository.getAll(search = search.ifBlank { null })
                Log.d("EditeurList", "Loaded ${list.size} editeurs")
                list.forEach { Log.d("EditeurList", "  - Editeur: ${it.nom}, ID: ${it.id}") }
                _uiState.value = _uiState.value.copy(isLoading = false, editeurs = list)
            } catch (e: Exception) {
                Log.e("EditeurList", "Error loading editeurs: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                editeurRepository.delete(id)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
