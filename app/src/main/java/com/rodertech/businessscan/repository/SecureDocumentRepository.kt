package com.rodertech.businessscan.repository

import android.graphics.Bitmap
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecureDocumentRepository {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding" // Especifica modo e padding seguros
    private val secretKey = SecretKeySpec("SuaChaveSecreta32BytesComprimentoXX!".toByteArray(), "AES")

    private fun encryptBitmap(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val rawBytes = baos.toByteArray()

        val cipher = Cipher.getInstance(ALGORITHM)

        // Gera um vetor de inicialização (IV) aleatório para maior segurança no modo CBC
        val ivBytes = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(rawBytes)

        // Combinamos o IV + Dados Criptografados para permitir a descriptografia futura se necessário
        val combined = ByteArray(ivBytes.size + encryptedBytes.size)
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.size)
        System.arraycopy(encryptedBytes, 0, combined, ivBytes.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun uploadEncryptedDocumentToFirebase(
        documentId: String,
        signedBitmap: Bitmap,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val encryptedBase64 = encryptBitmap(signedBitmap)
            val db = FirebaseFirestore.getInstance()

            val documentData = hashMapOf(
                "documentId" to documentId,
                "encryptedData" to encryptedBase64,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("encrypted_documents")
                .document(documentId)
                .set(documentData)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onError(exception) }

        } catch (e: Exception) {
            onError(e)
        }
    }
}

