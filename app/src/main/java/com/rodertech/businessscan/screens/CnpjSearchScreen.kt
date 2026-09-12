package com.rodertech.businessscan.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodertech.businessscan.model.Business
import com.rodertech.businessscan.util.PdfGenerator
import com.rodertech.businessscan.util.RewardedAdManager
import com.rodertech.businessscan.viewmodel.SearchUiState
import com.rodertech.businessscan.viewmodel.SearchViewModel
import com.rodertech.businessscan.data.UserPreferences

class CnpjVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 14) text.text.substring(0, 14) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 4) out += "."
            if (i == 7) out += "/"
            if (i == 11) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                if (offset <= 7) return offset + 2
                if (offset <= 11) return offset + 3
                if (offset <= 14) return offset + 4
                return out.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 6) return (offset - 1).coerceAtLeast(0)
                if (offset <= 10) return (offset - 2).coerceAtLeast(0)
                if (offset <= 15) return (offset - 3).coerceAtLeast(0)
                if (offset <= 18) return (offset - 4).coerceAtLeast(0)
                return trimmed.length
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CnpjSearchScreen(
    onBack: () -> Unit = {},
    onOpenPremium: (Business?) -> Unit = {},
    searchViewModel: SearchViewModel = viewModel()
) {
    var cnpjQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val userPreferences = remember { UserPreferences(context) }
    val isPremium by userPreferences.isPremiumFlow.collectAsState(initial = false)

    val rewardedManager = remember { RewardedAdManager(context) }
    var isConsultUnlockedByAd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        rewardedManager.loadAd()
    }

    val uiState by searchViewModel.uiState.collectAsState()
    val cleanDigits = cnpjQuery.filter { it.isDigit() }
    val isCnpjValid = cleanDigits.length == 14

    val backgroundColor = Color(0xFF1B1F38)
    val cardBackgroundColor = Color(0xFF282D4F)
    val buttonPurpleColor = Color(0xFF6C5CE7)
    val goldColor = Color(0xFFFFC107)

    fun executarBusca() {
        if (isCnpjValid) {
            keyboardController?.hide()
            searchViewModel.buscarPorCnpj(cleanDigits)
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(cardBackgroundColor, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Consulta de CNPJ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Consultar CNPJ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = cnpjQuery,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 14) {
                                cnpjQuery = digitsOnly
                            }
                        },
                        placeholder = { Text("Digite o CNPJ (14 dígitos)", color = Color.Gray) },
                        singleLine = true,
                        visualTransformation = CnpjVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { executarBusca() }),
                        trailingIcon = {
                            Text(
                                text = "Colar",
                                color = buttonPurpleColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable {
                                        runCatching {
                                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clipData = clipboardManager?.primaryClip
                                            clipData?.getItemAt(0)?.text?.toString() ?: ""
                                        }.onSuccess { copiedText ->
                                            val digits = copiedText.filter { it.isDigit() }
                                            if (digits.length <= 14) {
                                                cnpjQuery = digits
                                            }
                                        }
                                    }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = buttonPurpleColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { executarBusca() },
                        enabled = uiState !is SearchUiState.Loading && isCnpjValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonPurpleColor,
                            disabledContainerColor = Color(0xFF3F4568)
                        )
                    ) {
                        if (uiState is SearchUiState.Loading) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.alpha(pulseAlpha)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Consultando Escudo",
                                    tint = goldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CONSULTANDO...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "BUSCAR EMPRESA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            when (val state = uiState) {
                is SearchUiState.Success -> {
                    val business = state.business
                    val isAtiva = business.situacaoCadastral.equals("ATIVA", ignoreCase = true)
                    val statusColor = if (isAtiva) Color(0xFF2ECC71) else Color(0xFFE74C3C)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = business.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Situação Cadastral: ", color = Color.LightGray, fontSize = 13.sp)
                                Text(
                                    text = if (isAtiva) "ATIVA" else business.situacaoCadastral.ifEmpty { "INATIVA" },
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (isPremium || isConsultUnlockedByAd) {
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "✨ Consulta liberada por 1 uso via anúncio!",
                                        color = Color(0xFF2ECC71),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )

                                Text(
                                    text = "📊 Relatório Avançado Pro",
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (business.cnae.isNotEmpty()) {
                                    Text(
                                        text = "Atividade: ${business.cnae}",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "Capital Social: ${business.capitalSocialFormatado}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Porte Estimado: ${business.estimativaFaturamento}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )

                                if (business.endereco.isNotEmpty()) {
                                    Text(
                                        text = "Endereço: ${business.endereco}, ${business.municipio} - ${business.uf}",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "👥 Quadro de Sócios (QSA):",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                if (business.qsa.isEmpty()) {
                                    Text(
                                        text = "Nenhum sócio/administrador informado na Receita.",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    business.qsa.forEach { socio ->
                                        Text(
                                            text = "• ${socio.nome} (${socio.cargo.ifEmpty { "Sócio" }})",
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        PdfGenerator.generateBusinessReportPdf(context, business)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor)
                                ) {
                                    Text(
                                        text = "📄 BAIXAR RELATÓRIO EM PDF",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            rewardedManager.showAd {
                                                isConsultUnlockedByAd = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "📺 Assistir anúncio para liberar esta consulta",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onOpenPremium(business) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "👑 SEJA PREMIUM ILIMITADO",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Erro", tint = Color(0xFFF87171))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = state.message, color = Color(0xFFFECACA), fontSize = 13.sp)
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}