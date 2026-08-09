package com.example.business_scan.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    // Flow para observar o estado do checkbox "Lembrar-me"
    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMEMBER_ME] ?: false
    }

    // Flow para observar o e-mail salvo
    val savedEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL] ?: ""
    }

    // Salva ou remove o e-mail dependendo da escolha do usuário
    suspend fun saveUserSession(remember: Boolean, email: String) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME] = remember
            if (remember) {
                prefs[USER_EMAIL] = email
            } else {
                prefs.remove(USER_EMAIL)
            }
        }
    }

    // Limpa todas as preferências registradas (útil para botão de Logout)
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Leitura síncrona/direta do e-mail salvo (útil para preenchimento no arranque do app)
    suspend fun getSavedEmail(): String {
        val prefs = context.dataStore.data.first()
        return if (prefs[REMEMBER_ME] == true) prefs[USER_EMAIL] ?: "" else ""
    }
}