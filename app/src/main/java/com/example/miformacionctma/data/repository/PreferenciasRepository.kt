package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

// Preferencias del usuario expuestas de forma reactiva (interfaz para poder usar un doble en las pruebas)
interface PreferenciasUsuario {
    val mostrarSoloCompletadas: Flow<Boolean>
    suspend fun guardarFiltroCompletadas(mostrar: Boolean)
}

class PreferenciasRepository(private val context: Context) : PreferenciasUsuario {

    companion object {
        val FILTRO_COMPLETADAS = booleanPreferencesKey("filtro_completadas")
    }

    override val mostrarSoloCompletadas: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FILTRO_COMPLETADAS] ?: false
        }

    override suspend fun guardarFiltroCompletadas(mostrar: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FILTRO_COMPLETADAS] = mostrar
        }
    }
}