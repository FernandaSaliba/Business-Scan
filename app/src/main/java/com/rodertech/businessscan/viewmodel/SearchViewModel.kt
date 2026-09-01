package com.rodertech.businessscan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rodertech.businessscan.data.local.AppDatabase
import com.rodertech.businessscan.data.local.DocumentoOcrEntity
import com.rodertech.businessscan.model.Business
import com.rodertech.businessscan.network.RetrofitClient
import com.rodertech.businessscan.util.CryptoManager
import com.rodertech.businessscan.util.OcrHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Variável para guardar a empresa selecionada
    var selectedBusiness by mutableStateOf<Business?>(null)
        private set

    // Instância do Helper de OCR
    private val ocrHelper = OcrHelper()

    // Instância do DAO do Room para salvar o histórico de OCR
    private val dao = AppDatabase.getDatabase(getApplication()).documentOcrDao()

    // Estado para guardar o texto escaneado e atualizar a tela em tempo real
    var textoOcrResult by mutableStateOf("")
        private set

    var isProcessingOcr by mutableStateOf(false)
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

    // Função para processar o OCR a partir de um Bitmap
    fun processarOcr(bitmap: Bitmap) {
        isProcessingOcr = true
        ocrHelper.processarImagem(
            bitmap = bitmap,
            onSuccess = { texto ->
                textoOcrResult = texto
                isProcessingOcr = false

                // 1. Criptografa o texto extraído pelo OCR antes de salvar
                val textoCriptografado = CryptoManager.encrypt(texto)

                // 2. Salva o texto criptografado no banco de dados local (Room)
                viewModelScope.launch {
                    dao.insertDocumento(
                        DocumentoOcrEntity(textoExtraido = textoCriptografado)
                    )
                }

                // 3. Envia o texto criptografado para o Firestore (nuvem)
                salvarNoFirestore(textoCriptografado)
            },
            onError = { _ ->
                isProcessingOcr = false
            }
        )
    }

    private fun salvarNoFirestore(textoCriptografado: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val dados = hashMapOf(
            "userId" to userId,
            "conteudo" to textoCriptografado,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("documentos_ocr")
            .add(dados)
            .addOnSuccessListener {
                // Sucesso ao salvar na nuvem
            }
            .addOnFailureListener {
                // Tratamento de falha silencioso ou log se necessário
            }
    }

    // Função para resetar a busca (mantida para uso futuro)
    @Suppress("unused")
    fun resetSearch() {
        _uiState.value = SearchUiState.Idle
        selectedBusiness = null
        textoOcrResult = ""
    }
}