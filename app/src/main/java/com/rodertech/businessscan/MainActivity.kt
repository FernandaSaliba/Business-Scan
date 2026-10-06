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
import com.rodertech.businessscan.screens.SettingsScreen
import com.rodertech.businessscan.ui.theme.BusinessScanTheme
import androidx.core.content.FileProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.google.android.gms.ads.MobileAds.initialize(this) {}

        val userPreferences = UserPreferences(applicationContext)
        val googleWebClientId =
            "198083510769-287vessuvr02ggtmun2f02r2m5bnmunh.apps.googleusercontent.com"

        setContent {
            BusinessScanTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val rememberMeState by userPreferences.rememberMeFlow.collectAsState(initial = false)

                val isLoggedIn = remember { mutableStateOf(false) }
                val showSplash = remember { mutableStateOf(true) }

                val currentScreen = remember { mutableStateOf("search") }
                val selectedBusinessForPro = remember { mutableStateOf<Business?>(null) }

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

                        "document_capture" -> SignatureCaptureScreen(
                            onNavigateBack = { currentScreen.value = "search" },
                            // Ajustado para capturar os dois bitmaps enviados pelo ecrã de captura
                            onProceedToPlacement = { signatureBitmap, documentBitmap ->
                                signatureBitmapToUse.value = signatureBitmap
                                documentToSignBitmap.value = documentBitmap
                                currentScreen.value = "signature_placement"
                            }
                        )

                        "ocr" -> OcrScreen(
                            onNavigateBack = { currentScreen.value = "search" }
                        )

                        "signature_placement" -> {
                            val doc = documentToSignBitmap.value
                            val sig = signatureBitmapToUse.value
                            if (doc != null && sig != null) {
                                SignaturePlacementScreen(
                                    documentBitmap = doc,
                                    signatureBitmap = sig,
                                    onConfirmPosition = { offsetX, offsetY, width, height ->
                                        val signedBitmap = DocumentStamper.stampSignature(
                                            documentBitmap = doc,
                                            signatureBitmap = sig,
                                            screenOffsetX = offsetX,
                                            screenOffsetY = offsetY,
                                            displayedWidth = width,
                                            displayedHeight = height
                                        )

                                        val documentId = "doc_" + System.currentTimeMillis()

                                        SecureDocumentRepository.uploadEncryptedDocumentToFirebase(
                                            documentId = documentId,
                                            signedBitmap = signedBitmap,
                                            onSuccess = {},
                                            onError = {}
                                        )

                                        val cachePath = File(context.cacheDir, "images")
                                        cachePath.mkdirs()
                                        val file = File(cachePath, "documento_assinado_final.png")
                                        FileOutputStream(file).use { stream ->
                                            signedBitmap.compress(
                                                Bitmap.CompressFormat.PNG,
                                                100,
                                                stream
                                            )
                                        }

                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )

                                        val intent =
                                            android.content.Intent(android.content.Intent.ACTION_SEND)
                                                .apply {
                                                    type = "image/png"
                                                    putExtra(
                                                        android.content.Intent.EXTRA_STREAM,
                                                        uri
                                                    )
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                        context.startActivity(
                                            android.content.Intent.createChooser(
                                                intent,
                                                "Enviar documento assinado via:"
                                            )
                                        )

                                        currentScreen.value = "search"
                                    }
                                )
                            } else {
                                currentScreen.value = "document_capture"
                            }
                        }

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
                                currentScreen.value = "document_capture"
                            },
                            onNavigateToOcr = {
                                currentScreen.value = "ocr"
                            },
                            onNavigateToCnpjSearch = {
                                currentScreen.value = "cnpj_search"
                            },
                            onNavigateToSettings = {
                                currentScreen.value = "settings"
                            }
                        )

                        "settings" -> SettingsScreen(
                            onBackClick = { currentScreen.value = "search" },
                            onAccountDeleted = {
                                scope.launch {
                                    userPreferences.clearSession()
                                    FirebaseAuth.getInstance().signOut()
                                    isLoggedIn.value = false
                                    showSplash.value = true
                                    currentScreen.value = "search"
                                }
                            },
                            onOpenPremium = {
                                selectedBusinessForPro.value = null
                                currentScreen.value = "premium"
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
}