package com.example.business_scan.auth



import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.business_scan.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await


class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val credentialManager = CredentialManager.create(context)

// --- LOGIN COM GOOGLE ---
    suspend fun signInWithGoogle(
        webClientId: String = "",
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
    try {
        val clientId = webClientId.ifEmpty {
            runCatching { context.getString(R.string.default_web_client_id) }.getOrDefault("")
        }

        // Log de depuração para verificar se a Web Client ID está sendo carregada
        Log.d("AuthManager", "Client ID utilizado: '$clientId'")

        if (clientId.isEmpty()) {
            onError("Web Client ID não configurado no google-services.json ou via parâmetro.")
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Permite selecionar qualquer conta do dispositivo
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
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
        } catch (e: GetCredentialCancellationException) {
        // Usuário fechou a janela de login do Google
        onError("Login cancelado pelo usuário.")
        } catch (e: GetCredentialException) {
        Log.e("AuthManager", "Erro no CredentialManager", e)
        onError("Erro no Google: ${e.message} (Verifique a SHA-1 e a Web Client ID no Firebase)")
        } catch (e: Exception) {
        Log.e("AuthManager", "Erro genérico no login", e)
        onError(e.localizedMessage ?: "Erro na autenticação do Google.")
        }
    }



    fun logout() {

        auth.signOut()

    }



    fun getCurrentUser(): FirebaseUser? = auth.currentUser

}

