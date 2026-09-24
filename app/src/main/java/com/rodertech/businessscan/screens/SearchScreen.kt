package com.rodertech.businessscan.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodertech.businessscan.model.Business
import com.rodertech.businessscan.util.RewardedAdManager
import com.rodertech.businessscan.data.UserPreferences
import com.rodertech.businessscan.viewmodel.SearchUiState
import com.rodertech.businessscan.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onLogout: () -> Unit = {},
    onOpenPremium: (Business?) -> Unit = {},
    onNavigateToSignature: () -> Unit = {},
    onNavigateToCnpjSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOcr: () -> Unit = {},
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

    // Leitura correta do fluxo ou estado premium do UserPreferences
    val isPremium by userPreferences.isPremiumFlow.collectAsState(initial = false)

    val rewardedManager = remember { RewardedAdManager(context) }

    var showUnlockDialogFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        rewardedManager.loadAd()
    }

    val uiState by searchViewModel.uiState.collectAsState()
    val currentBusiness = (uiState as? SearchUiState.Success)?.business

    val backgroundColor = Color(0xFF1B1F38)
    val cardBackgroundColor = Color(0xFF282D4F)
    val buttonPurpleColor = Color(0xFF6C5CE7)
    val premiumCardBg = Color(0xFF1E223D)
    val goldColor = Color(0xFFFFC107)
    val orangeButtonColor = Color(0xFFE67E22)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Sair", color = Color.White, fontWeight = FontWeight.Normal, fontSize = 13.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BusinessScan",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isPremium) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = goldColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = goldColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Consulta e Inteligência Cadastral",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .background(cardBackgroundColor, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "🏢 Consulta de CNPJ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pesquise dados cadastrais, situação e QSA de empresas.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToCnpjSearch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor)
                        ) {
                            Text(
                                text = "ACESSAR CONSULTA DE CNPJ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📷 Digitalização Inteligente (OCR)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            if (!isPremium) {
                                Surface(
                                    color = goldColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = goldColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extraia textos e dados de documentos instantaneamente.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (isPremium) {
                                    onNavigateToOcr()
                                } else {
                                    showUnlockDialogFor = "OCR Inteligente"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor)
                        ) {
                            Text(
                                text = if (isPremium) "SELECIONAR DOCUMENTO PARA OCR" else "Reconhecimento Óptico de Caracteres",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        if (searchViewModel.isProcessingOcr) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = goldColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Lendo documento...", color = Color.LightGray, fontSize = 11.sp)
                        }

                        if (searchViewModel.textoOcrResult.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Texto extraído:",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = backgroundColor,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = searchViewModel.textoOcrResult,
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✍️ Assinatura Digital via RG",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            if (!isPremium) {
                                Surface(
                                    color = goldColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = goldColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cadastre sua assinatura para anexá-la automaticamente em relatórios.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (isPremium) {
                                    onNavigateToSignature()
                                } else {
                                    showUnlockDialogFor = "Assinatura Digital"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor)
                        ) {
                            Text(
                                text = if (isPremium) "CONFIGURAR ASSINATURA" else "Assinatura Digital",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isPremium) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = premiumCardBg),
                        border = BorderStroke(1.dp, goldColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ Vantagens do Plano Premium",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = goldColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val vantagens = listOf(
                                "Digitalização e Leitura Inteligente (OCR)",
                                "Assinatura Digital de documentos via RG",
                                "Análise completa de quadro sócio-administrador (QSA)",
                                "Estimativa de faturamento e faixa de capital social",
                                "Consultas ilimitadas sem anúncios"
                            )

                            vantagens.forEach { vantagem ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "✓ ",
                                        color = Color(0xFF2ECC71),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = vantagem,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onOpenPremium(currentBusiness) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orangeButtonColor)
                    ) {
                        Text(
                            text = "👑 SEJA PREMIUM AGORA",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showUnlockDialogFor != null) {
            UnlockDialog(
                featureTitle = showUnlockDialogFor!!,
                onDismiss = { showUnlockDialogFor = null },
                onWatchAd = {
                    val feature = showUnlockDialogFor
                    rewardedManager.showAd {
                        if (feature == "OCR Inteligente") {
                            onNavigateToOcr()
                        } else {
                            onNavigateToSignature()
                        }
                    }
                },
                onGoPremium = {
                    onOpenPremium(currentBusiness)
                }
            )
        }
    }
}