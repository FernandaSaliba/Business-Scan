package com.rodertech.businessscan.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await


@Suppress("unused")
class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // --- MENSAGENS DE ERRO AMIGÁVEIS ---
    fun parseAuthException(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."
            is FirebaseAuthInvalidUserException -> "Conta não encontrada. Crie uma nova conta."
            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
            else -> exception?.localizedMessage ?: "Ocorreu um erro inesperado. Tente novamente."
        }
    }

    // --- LOGIN COM E-MAIL E SENHA ---
    suspend fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            authResult.user?.let(onSuccess) ?: onError("Falha ao obter usuário.")
        } catch (e: Exception) {
            onError(parseAuthException(e))
        }
    }

    // --- CADASTRO COM E-MAIL E SENHA ---
    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            authResult.user?.let(onSuccess) ?: onError("Falha ao criar usuário.")
        } catch (e: Exception) {
            onError(parseAuthException(e))
        }
    }

    // --- REDEFINIÇÃO DE SENHA ---
    suspend fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            onSuccess()
        } catch (e: Exception) {
            onError(parseAuthException(e))
        }
    }

    // --- LOGIN COM GOOGLE ---
    suspend fun signInWithGoogle(
        webClientId: String = "198083510769-287vessuvr02ggtmun2f02r2m5bnmunh.apps.googleusercontent.com",
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Garantir nonce para segurança adicional e compatibilidade
            val rawNonce = java.util.UUID.randomUUID().toString()
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
            val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user?.let(onSuccess) ?: onError("Falha ao obter usuário do Firebase.")
            } else {
                onError("Tipo de credencial inválido.")
            }
        } catch (_: GetCredentialCancellationException) {
            // O utilizador cancelou a caixa de diálogo
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Erro no CredentialManager: ${e.errorMessage}", e)
            onError("Erro no Google: [${e.type}] ${e.message}")
        } catch (e: Exception) {
            Log.e("AuthManager", "Erro no login Google", e)
            onError(e.localizedMessage ?: "Erro na autenticação do Google.")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // --- EXCLUSÃO DE CONTA ---
    suspend fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val user = auth.currentUser ?: run {
                onError("Nenhum usuário logado.")
                return
            }
            user.delete().await()
            auth.signOut()
            onSuccess()
        } catch (e: Exception) {
            val message = if (e.message?.contains("recent-login") == true) {
                "Por segurança, faça login novamente antes de excluir a conta."
            } else {
                e.localizedMessage ?: "Erro ao excluir a conta."
            }
            onError(message)
        }
    }
}