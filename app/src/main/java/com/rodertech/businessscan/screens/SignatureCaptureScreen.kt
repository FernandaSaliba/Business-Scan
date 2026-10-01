package com.rodertech.businessscan.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
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

private val darkBg = Color(0xFF0F172A)
private val cardBg = Color(0xFF1E293B)
private val primaryText = Color.White
private val accentColor = Color(0xFF818CF8)
private val buttonGold = Color(0xFFD97706)

@Composable
fun SignatureCaptureScreen(
    onNavigateBack: () -> Unit,
    onSignatureReady: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Botão de voltar padronizado com efeito de toque
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
                text = "Selecione a origem da assinatura ou documento para capturar a imagem com alta nitidez.",
                fontSize = 14.sp,
                color = Color.LightGray
            )

            // Botões de Ação (Câmara e Galeria) com efeito de toque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val camInteractionSource = remember { MutableInteractionSource() }
                val isCamPressed by camInteractionSource.collectIsPressedAsState()
                val camScale by animateFloatAsState(targetValue = if (isCamPressed) 0.96f else 1f, label = "camScale")

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .scale(camScale),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonGold),
                    interactionSource = camInteractionSource
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tirar Foto", fontWeight = FontWeight.Bold, color = Color.White)
                }

                val galInteractionSource = remember { MutableInteractionSource() }
                val isGalPressed by galInteractionSource.collectIsPressedAsState()
                val galScale by animateFloatAsState(targetValue = if (isGalPressed) 0.96f else 1f, label = "galScale")

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .scale(galScale),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    border = BorderStroke(1.dp, accentColor),
                    interactionSource = galInteractionSource
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Galeria", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Pré-visualização da Assinatura/Documento
            val currentBitmap = capturedBitmap
            if (currentBitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = currentBitmap.asImageBitmap(),
                            contentDescription = "Assinatura capturada",
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    }
                }

                // Botão de Confirmar Assinatura com efeito de toque
                val confInteractionSource = remember { MutableInteractionSource() }
                val isConfPressed by confInteractionSource.collectIsPressedAsState()
                val confScale by animateFloatAsState(targetValue = if (isConfPressed) 0.96f else 1f, label = "confScale")

                Button(
                    onClick = { onSignatureReady(currentBitmap) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(confScale),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    interactionSource = confInteractionSource
                ) {
                    Text(text = "CONFIRMAR ASSINATURA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhuma imagem selecionada.\nTire uma foto ou escolha da galeria.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}