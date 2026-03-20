package com.example.frontend_mobile_etape1.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_mobile_etape1.core.auth.AuthManager
import com.example.frontend_mobile_etape1.core.network.RetrofitInstance
import com.example.frontend_mobile_etape1.data.dto.FestivalDto
import com.example.frontend_mobile_etape1.data.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val latestFestival: FestivalDto? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val festivalRepository = FestivalRepository(RetrofitInstance.festivalApi)
    val authManager = AuthManager(RetrofitInstance.authApi, RetrofitInstance.cookieJar)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadLatestFestival()
    }

    private fun loadLatestFestival() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val festivals = festivalRepository.getAll()
                // Premier festival de la liste (trié par created_at DESC côté backend)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    latestFestival = festivals.firstOrNull()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
