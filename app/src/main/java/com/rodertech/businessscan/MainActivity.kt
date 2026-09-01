package com.rodertech.businessscan

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.rodertech.businessscan.data.UserPreferences
import com.rodertech.businessscan.model.Business
import com.rodertech.businessscan.screens.DocumentCaptureScreen
import com.rodertech.businessscan.screens.HomeScreen
import com.rodertech.businessscan.screens.LoginScreen
import com.rodertech.businessscan.screens.OcrScreen
import com.rodertech.businessscan.screens.PremiumScreen
import com.rodertech.businessscan.screens.SearchScreen
import com.rodertech.businessscan.screens.SignatureCaptureScreen
import com.rodertech.businessscan.screens.SignaturePlacementScreen
import com.rodertech.businessscan.screens.SplashScreen
import com.rodertech.businessscan.util.DocumentStamper
import com.rodertech.businessscan.repository.SecureDocumentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.rodertech.businessscan.screens.CnpjSearchScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(applicationContext)
        val googleWebClientId = "198083510769-287vessuvr02ggtmun2f02r2m5bnmunh.apps.googleusercontent.com"

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val rememberMeState by userPreferences.rememberMeFlow.collectAsState(initial = false)

            val isLoggedIn = remember { mutableStateOf(false) }
            val showSplash = remember { mutableStateOf(true) }

            // Inicia direto na tela de busca ("search")
            val currentScreen = remember { mutableStateOf("search") }
            val selectedBusinessForPro = remember { mutableStateOf<Business?>(null) }

            // Estados temporários para guardar os Bitmaps durante o fluxo de assinatura
            val documentToSignBitmap = remember { mutableStateOf<Bitmap?>(null) }
            val signatureBitmapToUse = remember { mutableStateOf<Bitmap?>(null) }

            LaunchedEffect(rememberMeState) {
                if (rememberMeState || FirebaseAuth.getInstance().currentUser != null) {
                    isLoggedIn.value = true
                }
            }

            if (showSplash.value) {
                SplashScreen(onTimeout = {
                    showSplash.value = false
                })
            } else if (!isLoggedIn.value) {
                LoginScreen(
                    webClientId = googleWebClientId,
                    onLoginSuccess = { rememberUser, email ->
                        scope.launch {
                            userPreferences.saveUserSession(rememberUser, email)
                            isLoggedIn.value = true
                        }
                    }
                )
            } else {
                when (currentScreen.value) {
                    "home" -> HomeScreen()
                    "signature" -> SignatureCaptureScreen(
                        onNavigateBack = { currentScreen.value = "search" }
                    )
                    "document_capture" -> DocumentCaptureScreen(
                        onDocumentCaptured = { docBitmap, sigBitmap ->
                            documentToSignBitmap.value = docBitmap
                            signatureBitmapToUse.value = sigBitmap
                            currentScreen.value = "signature_placement"
                        },
                        onNavigateToCaptureSignature = {
                            currentScreen.value = "signature"
                        }
                    )
                    "signature_placement" -> {
                        val doc = documentToSignBitmap.value
                        val sig = signatureBitmapToUse.value
                        if (doc != null && sig != null) {
                            SignaturePlacementScreen(
                                documentBitmap = doc,
                                signatureBitmap = sig,
                                onConfirmPosition = { offsetX, offsetY, width, height ->
                                    // 1. Carimba o documento na posição escolhida
                                    val signedBitmap = DocumentStamper.stampSignature(
                                        documentBitmap = doc,
                                        signatureBitmap = sig,
                                        screenOffsetX = offsetX,
                                        screenOffsetY = offsetY,
                                        displayedWidth = width,
                                        displayedHeight = height
                                    )

                                    val documentId = "doc_" + System.currentTimeMillis()

                                    // 2. Envia criptografado para o Firebase Storage
                                    SecureDocumentRepository.uploadEncryptedDocumentToFirebase(
                                        documentId = documentId,
                                        signedBitmap = signedBitmap,
                                        onSuccess = { /* Sucesso na nuvem */ },
                                        onError = { /* Tratar erro se necessário */ }
                                    )

                                    // 3. Salva e abre o compartilhamento imediato
                                    val cachePath = File(context.cacheDir, "images")
                                    cachePath.mkdirs()
                                    val file = File(cachePath, "documento_assinado_final.png")
                                    FileOutputStream(file).use { stream ->
                                        signedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                    }

                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )

                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Enviar documento assinado via:"))

                                    // Retorna para a busca após concluir
                                    currentScreen.value = "search"
                                }
                            )
                        } else {
                            currentScreen.value = "document_capture"
                        }
                    }
                    "ocr" -> OcrScreen(
                        onNavigateBack = { currentScreen.value = "search" }
                    )
                    "search" -> SearchScreen(
                        onLogout = {
                            scope.launch {
                                userPreferences.clearSession()
                                FirebaseAuth.getInstance().signOut()
                                isLoggedIn.value = false
                                showSplash.value = true
                            }
                        },
                        onOpenPremium = { business ->
                            selectedBusinessForPro.value = business
                            currentScreen.value = "premium"
                        },
                        onNavigateToSignature = {
                            // Agora aponta para iniciar o fluxo completo de escanear o documento a ser assinado
                            currentScreen.value = "document_capture"
                        },
                        onNavigateToCnpjSearch = {
                            currentScreen.value = "cnpj_search"
                        }
                    )
                    "cnpj_search" -> CnpjSearchScreen(
                        onBack = { currentScreen.value = "search" },
                        onOpenPremium = { business ->
                            selectedBusinessForPro.value = business
                            currentScreen.value = "premium"
                        }
                    )
                    "premium" -> PremiumScreen(
                        business = selectedBusinessForPro.value,
                        onBackClick = {
                            currentScreen.value = "search"
                        },
                        onSubscribeSuccess = {
                            currentScreen.value = "search"
                        }
                    )
                }
            }
        }
    }
}