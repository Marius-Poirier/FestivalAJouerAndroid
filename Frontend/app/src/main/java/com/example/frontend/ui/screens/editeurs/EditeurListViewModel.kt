package com.example.frontend.ui.screens.editeurs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.auth.AuthManager
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.EditeurDto
import com.example.frontend.data.repository.EditeurRepository
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
    val authManager = RetrofitInstance.authManager

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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = editeurRepository.getAll(search = search.ifBlank { null })
                _uiState.value = _uiState.value.copy(isLoading = false, editeurs = list)
            } catch (e: Exception) {
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
