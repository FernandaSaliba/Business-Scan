package com.example.business_scan.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OcrHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processarImagem(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}