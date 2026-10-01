package com.rodertech.businessscan.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodertech.businessscan.util.OcrHelper
import androidx.compose.foundation.BorderStroke

private val darkBg = Color(0xFF0F172A)
private val cardBg = Color(0xFF1E293B)
private val primaryText = Color.White
private val accentColor = Color(0xFF818CF8)
private val buttonGold = Color(0xFFD97706)

@Composable
fun OcrScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val ocrHelper = remember { OcrHelper() }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("Nenhum texto extraído ainda. Tire a foto ou escolha um documento da galeria.") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isLoading = true
            ocrHelper.processarImagem(
                bitmap = bitmap,
                onSuccess = { text ->
                    extractedText = text.ifBlank { "Nenhum texto identificado na imagem." }
                    isLoading = false
                },
                onError = { exception ->
                    extractedText = "Erro ao processar OCR: ${exception.localizedMessage}"
                    isLoading = false
                }
            )
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            capturedBitmap = bitmap
            isLoading = true
            ocrHelper.processarImagem(
                bitmap = bitmap,
                onSuccess = { text ->
                    extractedText = text.ifBlank { "Nenhum texto identificado na imagem." }
                    isLoading = false
                },
                onError = { exception ->
                    extractedText = "Erro ao processar OCR: ${exception.localizedMessage}"
                    isLoading = false
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBg)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Botão de voltar com efeito de toque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                val backInteractionSource = remember { MutableInteractionSource() }
                val isBackPressed by backInteractionSource.collectIsPressedAsState()
                val backScale by animateFloatAsState(targetValue = if (isBackPressed) 0.92f else 1f, label = "backScale")

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(backScale)
                        .background(cardBg, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = backInteractionSource,
                            indication = null,
                            onClick = onNavigateBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = primaryText
                    )
                }
            }

            Text(
                text = "Digitalização Inteligente (OCR)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                modifier = Modifier.align(Alignment.Start)
            )

            // Botões de Ação (Tirar Foto e Galeria) com efeito de toque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val camInteractionSource = remember { MutableInteractionSource() }
                val isCamPressed by camInteractionSource.collectIsPressedAsState()
                val camScale by animateFloatAsState(targetValue = if (isCamPressed) 0.96f else 1f, label = "camScale")

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .scale(camScale),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonGold),
                    interactionSource = camInteractionSource
                ) {
                    Text(text = "📷 Tirar Foto", fontWeight = FontWeight.Bold, color = Color.White)
                }

                val galInteractionSource = remember { MutableInteractionSource() }
                val isGalPressed by galInteractionSource.collectIsPressedAsState()
                val galScale by animateFloatAsState(targetValue = if (isGalPressed) 0.96f else 1f, label = "galScale")

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .scale(galScale),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    border = BorderStroke(1.dp, accentColor),
                    interactionSource = galInteractionSource
                ) {
                    Text(text = "🖼️ Galeria", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            val currentBitmap = capturedBitmap
            if (currentBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = currentBitmap.asImageBitmap(),
                            contentDescription = "Documento capturado",
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }
                }
            }

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
                        CircularProgressIndicator(
                            color = accentColor,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Pesquisar palavra no texto...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = darkBg,
                                unfocusedContainerColor = darkBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val displayedText = if (searchQuery.isBlank()) {
                            extractedText
                        } else {
                            extractedText.lines()
                                .filter { it.contains(searchQuery, ignoreCase = true) }
                                .joinToString("\n").ifBlank { "Nenhuma linha encontrada com o termo \"$searchQuery\"." }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 300.dp)
                                .background(darkBg, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = displayedText,
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}