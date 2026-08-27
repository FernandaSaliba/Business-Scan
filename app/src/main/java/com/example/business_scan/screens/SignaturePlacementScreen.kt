package com.example.business_scan.screens

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

@Composable
fun SignaturePlacementScreen(
    documentBitmap: Bitmap,
    signatureBitmap: Bitmap,
    onConfirmPosition: (offsetX: Float, offsetY: Float, displayedWidth: Float, displayedHeight: Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(100f) }
    var offsetY by remember { mutableFloatStateOf(100f) }

    var boxWidth by remember { mutableFloatStateOf(1f) }
    var boxHeight by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        // 1. Exibe o documento escaneado ao fundo
        Image(
            bitmap = documentBitmap.asImageBitmap(),
            contentDescription = "Documento Escaneado",
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    boxWidth = size.width.toFloat()
                    boxHeight = size.height.toFloat()
                }
        )

        // 2. Exibe a assinatura arrastável
        Image(
            bitmap = signatureBitmap.asImageBitmap(),
            contentDescription = "Assinatura Arrastável",
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(160.dp, 80.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        )

        // 3. Botão de confirmação
        Button(
            onClick = {
                onConfirmPosition(offsetX, offsetY, boxWidth, boxHeight)
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

