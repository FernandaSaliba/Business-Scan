package com.rodertech.businessscan.viewmodel



import com.rodertech.businessscan.model.Business



sealed interface SearchUiState {

    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Success(val business: Business) : SearchUiState

    data class Error(val message: String) : SearchUiState

}