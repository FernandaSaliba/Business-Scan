package com.example.business_scan.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.business_scan.util.OcrHelper

private val darkBg = Color(0xFF0F172A)
private val cardBg = Color(0xFF1E293B)
private val primaryText = Color.White
private val secondaryText = Color.Gray
private val accentColor = Color(0xFF818CF8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    onNavigateBack: () -> Unit
) {
    val ocrHelper = remember { OcrHelper() }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("Nenhum texto extraído ainda. Tire a foto de um documento.") }
    var isLoading by remember { mutableStateOf(false) }

    // Launcher para abrir a câmera nativa e capturar a prévia da imagem
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isLoading = true
            ocrHelper.processarImagem(
                bitmap = bitmap,
                onSuccess = { text ->
                    extractedText = if (text.isBlank()) "Nenhum texto identificado na imagem." else text
                    isLoading = false
                },
                onError = { exception ->
                    extractedText = "Erro ao processar OCR: ${exception.localizedMessage}"
                    isLoading = false
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digitalização Inteligente (OCR)", color = primaryText) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Voltar", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botão para disparar a câmera
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(text = "📷 Tirar Foto do Documento", fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Exibição da imagem capturada (se houver)
            if (capturedBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Documento capturado",
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }
                }
            }

            // Card com o resultado do OCR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Texto Reconhecido:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = accentColor, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Text(
                            text = extractedText,
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
