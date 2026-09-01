package com.rodertech.businessscan.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    // A chave precisa ter exatamente 16, 24 ou 32 caracteres (Ex: 16 bytes para AES-128)
    private const val ALGORITHM = "AES"
    private const val SECRET_KEY = "BusinessScanKey16" // Substitua por uma chave forte sua

    fun encrypt(plainText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText // Fallback caso ocorra algum erro
        }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText // Retorna o texto original caso falhe
        }
    }
}

