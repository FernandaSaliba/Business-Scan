package com.example.business_scan.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.business_scan.util.SignatureManager

@Composable
fun DocumentCaptureScreen(
    onDocumentCaptured: (Bitmap, Bitmap) -> Unit, // Passa o documento e a assinatura salva
    onNavigateToCaptureSignature: () -> Unit      // Caso não tenha assinatura cadastrada
) {
    val context = LocalContext.current
    var documentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher para selecionar o documento a ser assinado da galeria/câmera
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            documentBitmap = bitmap
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Assinar Documento",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Verifica se o usuário já cadastrou a assinatura antes
                    if (SignatureManager.hasSignature(context)) {
                        documentLauncher.launch("image/*") // Seleciona o documento alvo
                    } else {
                        onNavigateToCaptureSignature() // Redireciona para cadastrar a assinatura primeiro
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selecionar Documento para Assinar")
            }

            // Se o documento foi selecionado E a assinatura existe, chama a próxima etapa
            documentBitmap?.let { docBitmap ->
                val savedSignature = SignatureManager.getSavedSignature(context)
                if (savedSignature != null) {
                    LaunchedEffect(docBitmap) {
                        onDocumentCaptured(docBitmap, savedSignature)
                    }
                }
            }
        }
    }
}

