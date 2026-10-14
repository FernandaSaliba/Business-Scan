package com.rodertech.businessscan.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.graphics.createBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureCaptureScreen(
    onNavigateBack: () -> Unit,
    onProceedToPlacement: (Bitmap, Bitmap) -> Unit
) {
    val context = LocalContext.current

    var documentUri by remember { mutableStateOf<Uri?>(null) }

    // Controle do desenho da assinatura
    val path = remember { mutableStateOf(Path()) }
    var isSignatureDrawn by remember { mutableStateOf(false) }

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

            // Card 1: Desenhar Assinatura na Tela
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
                            text = "1. Desenhe sua Assinatura",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = goldColor
                        )

                        // Botão para limpar a assinatura
                        IconButton(
                            onClick = {
                                path.value = Path()
                                isSignatureDrawn = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Limpar",
                                tint = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use o dedo ou caneta stylus para assinar no quadro branco abaixo.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Quadro de Desenho (Canvas)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            path.value.moveTo(offset.x, offset.y)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            path.value.lineTo(change.position.x, change.position.y)
                                            // Força a recomposição do Canvas para desenhar o traço em tempo real
                                            path.value = Path().apply { addPath(path.value) }
                                            isSignatureDrawn = true
                                        }
                                    )
                                }
                        ) {
                            drawPath(
                                path = path.value,
                                color = Color.Black,
                                style = Stroke(width = 6f)
                            )
                        }
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
                            val currentUri = documentUri

                            if (!isSignatureDrawn || currentUri == null) {
                                Toast.makeText(context, "Desenhe a assinatura e escolha um documento primeiro.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            try {
                                // 1. Converte o desenho (Path do Canvas) em um Bitmap transparente
                                val signatureBitmap = generateBitmapFromPath(path.value)

                                // 2. Abre o documento selecionado
                                val inputStream = context.contentResolver.openInputStream(currentUri)
                                val docBitmap = BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()

                                if (docBitmap != null) {
                                    onProceedToPlacement(signatureBitmap, docBitmap)
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

// Função auxiliar para converter o desenho em um Bitmap limpo e transparente
private fun generateBitmapFromPath(
    path: Path,
    width: Int = 600,
    height: Int = 300
): Bitmap {
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    canvas.drawPath(path.asAndroidPath(), paint)
    return bitmap
}