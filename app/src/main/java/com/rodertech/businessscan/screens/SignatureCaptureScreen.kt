package com.rodertech.businessscan.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureCaptureScreen(
    onNavigateBack: () -> Unit,
    onProceedToPlacement: (Bitmap, Bitmap) -> Unit // <--- Alterado para receber assinatura e documento juntos
) {
    val context = LocalContext.current

    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var documentUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            signatureBitmap = bitmap
            Toast.makeText(context, "Assinatura capturada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para escolher o documento base na galeria
    val documentGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            documentUri = uri
            Toast.makeText(context, "Documento selecionado!", Toast.LENGTH_SHORT).show()
        }
    }

    val backgroundColor = Color(0xFF1B1F38)
    val cardBackgroundColor = Color(0xFF282D4F)
    val buttonPurpleColor = Color(0xFF6C5CE7)
    val goldColor = Color(0xFFFFC107)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Cabeçalho com botão de voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                val isBackPressed by backInteraction.collectIsPressedAsState()
                val backScale by animateFloatAsState(targetValue = if (isBackPressed) 0.92f else 1f, label = "backScale")

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(backScale)
                        .background(cardBackgroundColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = backInteraction,
                            indication = null,
                            onClick = onNavigateBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Assinatura Digital de Documentos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Card 1: Capturar Assinatura
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "1. Capturar Assinatura (Foto/RG)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = goldColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tire uma foto nítida da sua assinatura em um papel branco ou documento.", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val signCamInteraction = remember { MutableInteractionSource() }
                    val isSignCamPressed by signCamInteraction.collectIsPressedAsState()
                    val signCamScale by animateFloatAsState(targetValue = if (isSignCamPressed) 0.96f else 1f, label = "signCamScale")

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .scale(signCamScale),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor),
                        interactionSource = signCamInteraction
                    ) {
                        Text("📷 Tirar Foto da Assinatura", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (signatureBitmap != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            bitmap = signatureBitmap!!.asImageBitmap(),
                            contentDescription = "Assinatura capturada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            // Card 2: Selecionar Documento Destino
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "2. Selecionar Documento", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = goldColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Escolha o documento na galeria onde a assinatura será aplicada.", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val docInteraction = remember { MutableInteractionSource() }
                    val isDocPressed by docInteraction.collectIsPressedAsState()
                    val docScale by animateFloatAsState(targetValue = if (isDocPressed) 0.96f else 1f, label = "docScale")

                    Button(
                        onClick = { documentGalleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .scale(docScale),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonPurpleColor),
                        interactionSource = docInteraction
                    ) {
                        Text("📁 Escolher Documento da Galeria", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Card 3: Avançar para Posicionamento
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "3. Posicionar e Assinar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = goldColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Avance para posicionar a assinatura exatamente onde desejar no documento.", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val saveInteraction = remember { MutableInteractionSource() }
                    val isSavePressed by saveInteraction.collectIsPressedAsState()
                    val saveScale by animateFloatAsState(targetValue = if (isSavePressed) 0.96f else 1f, label = "saveScale")

                    Button(
                        onClick = {
                            val currentSig = signatureBitmap
                            val currentUri = documentUri

                            if (currentSig == null || currentUri == null) {
                                Toast.makeText(context, "Tire a foto da assinatura e escolha um documento primeiro.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            try {
                                val inputStream = context.contentResolver.openInputStream(currentUri)
                                val docBitmap = BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()

                                if (docBitmap != null) {
                                    // Envia para a MainActivity transacionar para o ecrã de posicionamento
                                    onProceedToPlacement(currentSig, docBitmap)
                                } else {
                                    Toast.makeText(context, "Erro ao carregar o documento.", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .scale(saveScale),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                        interactionSource = saveInteraction
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AVANÇAR PARA POSICIONAR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}