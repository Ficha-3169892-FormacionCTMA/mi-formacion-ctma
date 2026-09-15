package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferenciasRepository(private val context: Context) {

    companion object {
        val FILTRO_COMPLETADAS = booleanPreferencesKey("filtro_completadas")
    }

    val mostrarSoloCompletadas: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FILTRO_COMPLETADAS] ?: false
        }

    suspend fun guardarFiltroCompletadas(mostrar: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FILTRO_COMPLETADAS] = mostrar
        }
    }
}