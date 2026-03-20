package com.example.frontend_mobile_etape1.ui.screens.jeux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.dto.TypeJeuDto
import com.example.frontend_mobile_etape1.data.repository.JeuRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JeuListUiState(
    val isLoading: Boolean = false,
    val allJeux: List<JeuDto> = emptyList(),
    val filteredJeux: List<JeuDto> = emptyList(),
    val typesJeu: List<TypeJeuDto> = emptyList(),
    val selectedTypeId: Int? = null,  // null = "Tous"
    val searchQuery: String = "",
    val error: String? = null
)

@OptIn(FlowPreview::class)
class JeuListViewModel : ViewModel() {
    private val jeuRepository = JeuRepository(RetrofitInstance.jeuApi, RetrofitInstance.metadataApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(JeuListUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadTypes()
        load()
        // Debounce recherche 300ms
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    _uiState.value = _uiState.value.copy(searchQuery = query)
                    applyFilters()
                }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = jeuRepository.getAll()
                _uiState.value = _uiState.value.copy(isLoading = false, allJeux = list)
                applyFilters()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            try {
                val types = jeuRepository.getTypesJeu()
                _uiState.value = _uiState.value.copy(typesJeu = types)
            } catch (_: Exception) {}
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onTypeFilter(typeId: Int?) {
        _uiState.value = _uiState.value.copy(selectedTypeId = typeId)
        applyFilters()
    }

    private fun applyFilters() {
        val s = _uiState.value
        var filtered = s.allJeux
        if (s.searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.nom.contains(s.searchQuery, ignoreCase = true) }
        }
        if (s.selectedTypeId != null) {
            filtered = filtered.filter { it.typeJeuId == s.selectedTypeId }
        }
        _uiState.value = _uiState.value.copy(filteredJeux = filtered)
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                jeuRepository.delete(id)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
