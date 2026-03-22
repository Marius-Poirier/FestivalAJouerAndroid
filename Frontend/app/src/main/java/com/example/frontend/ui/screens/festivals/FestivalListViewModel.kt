package com.example.frontend.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.FestivalDto
import com.example.frontend.data.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FestivalListUiState(
    val isLoading: Boolean = false,
    val festivals: List<FestivalDto> = emptyList(),
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

class FestivalListViewModel : ViewModel() {
    private val festivalRepository = FestivalRepository(RetrofitInstance.festivalApi)
    val authManager = RetrofitInstance.authManager

    private val _uiState = MutableStateFlow(FestivalListUiState())
    val uiState = _uiState.asStateFlow()


    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = festivalRepository.getAll()
                _uiState.value = _uiState.value.copy(isLoading = false, festivals = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                festivalRepository.delete(id)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
            }
        }
    }
}
