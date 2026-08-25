package com.example.business_scan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object SignatureManager {
    private const val SIGNATURE_FILE_NAME = "saved_signature.png"

    fun saveSignature(context: Context, bitmap: Bitmap) {
        val file = File(context.filesDir, SIGNATURE_FILE_NAME)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    fun getSavedSignature(context: Context): Bitmap? {
        val file = File(context.filesDir, SIGNATURE_FILE_NAME)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun hasSignature(context: Context): Boolean {
        val file = File(context.filesDir, SIGNATURE_FILE_NAME)
        return file.exists()
    }
}

