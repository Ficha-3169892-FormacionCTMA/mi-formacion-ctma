package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "preferencias"
)

class PreferenciasRepository(
    private val context: Context
) {

    private companion object {
        val ORDEN_POR_PRIORIDAD = booleanPreferencesKey("orden_por_prioridad")
    }

    val ordenarPorPrioridad: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[ORDEN_POR_PRIORIDAD] ?: false
        }

    suspend fun guardarOrdenPorPrioridad(valor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ORDEN_POR_PRIORIDAD] = valor
        }
    }
}
