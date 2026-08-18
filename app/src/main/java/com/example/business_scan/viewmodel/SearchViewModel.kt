package com.example.business_scan.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.business_scan.model.Business
import com.example.business_scan.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Variável para guardar a empresa selecionada
    var selectedBusiness by mutableStateOf<Business?>(null)
        private set

    // Função para mudar a empresa manualmente (mantida para uso futuro)
    @Suppress("unused")
    fun selectBusiness(business: Business?) {
        selectedBusiness = business
    }

    fun buscarPorCnpj(cnpjInput: String) {
        val cleanCnpj = cnpjInput.filter { it.isDigit() }

        if (cleanCnpj.length != 14) {
            _uiState.value = SearchUiState.Error("Digite um CNPJ válido com 14 dígitos.")
            return
        }

        _uiState.value = SearchUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.buscarCnpj(cleanCnpj)

                if (response.isSuccessful && response.body() != null) {
                    val apiEmpresa = response.body()!!
                    val businessObj = apiEmpresa.toBusiness()

                    // Guarda a empresa automaticamente ao encontrar
                    selectedBusiness = businessObj

                    _uiState.value = SearchUiState.Success(businessObj)
                } else {
                    _uiState.value = SearchUiState.Error("CNPJ não encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Erro de conexão: ${e.localizedMessage ?: "Falha na busca"}")
            }
        }
    }

    // Função para resetar a busca (mantida para uso futuro)
    @Suppress("unused")
    fun resetSearch() {
        _uiState.value = SearchUiState.Idle
        selectedBusiness = null
    }
}