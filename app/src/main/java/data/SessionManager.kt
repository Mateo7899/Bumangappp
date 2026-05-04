package com.example.bumangapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos la instancia de DataStore
private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        // La "llave" para nuestra caja fuerte
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // Función para guardar que el usuario ya entró
    suspend fun saveLoginState(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    // Función para leer si el usuario está logueado (devuelve un Flow/Flujo de datos)
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false // Por defecto es falso
    }
}