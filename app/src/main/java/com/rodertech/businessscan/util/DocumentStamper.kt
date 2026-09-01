package com.rodertech.businessscan.util

import android.graphics.Bitmap
import android.graphics.Canvas

object DocumentStamper {

    fun stampSignature(
        documentBitmap: Bitmap,
        signatureBitmap: Bitmap,
        screenOffsetX: Float,
        screenOffsetY: Float,
        displayedWidth: Float,
        displayedHeight: Float
    ): Bitmap {
        val resultBitmap = documentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        // Calcula a proporção entre a imagem real de alta resolução e o tamanho exibido na tela
        val scaleX = documentBitmap.width.toFloat() / displayedWidth
        val scaleY = documentBitmap.height.toFloat() / displayedHeight

        // Mapeia a posição da tela para a coordenada real
        val realX = screenOffsetX * scaleX
        val realY = screenOffsetY * scaleY

        // Redimensiona a assinatura proporcionalmente ao documento real
        val targetSignatureWidth = (160f * scaleX).toInt()
        val ratio = targetSignatureWidth.toFloat() / signatureBitmap.width.toFloat()
        val targetSignatureHeight = (signatureBitmap.height * ratio).toInt()

        val scaledSignature = Bitmap.createScaledBitmap(
            signatureBitmap,
            targetSignatureWidth,
            targetSignatureHeight,
            true
        )

        // Carimba a assinatura no Canvas
        canvas.drawBitmap(scaledSignature, realX, realY, null)

        return resultBitmap
    }
}