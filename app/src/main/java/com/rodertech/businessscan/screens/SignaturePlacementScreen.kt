package com.rodertech.businessscan.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale

@Composable
fun SignaturePlacementScreen(
    documentBitmap: Bitmap,
    signatureBitmap: Bitmap,
    onConfirmPosition: (offsetX: Float, offsetY: Float, displayedWidth: Float, displayedHeight: Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(100f) }
    var offsetY by remember { mutableFloatStateOf(100f) }

    var displayedWidth by remember { mutableFloatStateOf(1f) }
    var displayedHeight by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        // 1. Exibe o documento escaneado ao fundo com cálculo correto das dimensões visíveis
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize ->
                    val containerW = containerSize.width.toFloat()
                    val containerH = containerSize.height.toFloat()

                    val bitmapW = documentBitmap.width.toFloat()
                    val bitmapH = documentBitmap.height.toFloat()

                    val containerRatio = containerW / containerH
                    val bitmapRatio = bitmapW / bitmapH

                    if (containerRatio > bitmapRatio) {
                        displayedHeight = containerH
                        displayedWidth = containerH * bitmapRatio
                    } else {
                        displayedWidth = containerW
                        displayedHeight = containerW / bitmapRatio
                    }
                }
        ) {
            Image(
                bitmap = documentBitmap.asImageBitmap(),
                contentDescription = "Documento Escaneado",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Exibe a assinatura arrastável posicionada sobre o documento
            Image(
                bitmap = signatureBitmap.asImageBitmap(),
                contentDescription = "Assinatura Arrastável",
                contentScale = ContentScale.Fit, // Preserva todos os limites do desenho
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(200.dp, 100.dp) // Proporção 2:1 idêntica à do Bitmap gerado (600x300)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            )}

        // 3. Botão de confirmação
        Button(
            onClick = {
                onConfirmPosition(offsetX, offsetY, displayedWidth, displayedHeight)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("Confirmar Posição e Assinar")
        }
    }
}

