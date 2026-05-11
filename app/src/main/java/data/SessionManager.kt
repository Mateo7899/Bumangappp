package com.example.bumangapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val IS_PREMIUM = booleanPreferencesKey("is_premium") // NUEVO
    }

    suspend fun saveLoginState(isLoggedIn: Boolean, email: String = "", isPremium: Boolean = false) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            preferences[USER_EMAIL] = email
            preferences[IS_PREMIUM] = isPremium // NUEVO
        }
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PREMIUM] ?: false
    }

    // Leer si está logueado
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    // Leer el email guardado
    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL] ?: ""
    }
}