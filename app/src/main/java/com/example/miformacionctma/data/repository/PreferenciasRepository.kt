package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "preferencias"
)

open class PreferenciasRepository(
    private val context: Context?
) {

    private companion object {
        val ORDEN_POR_PRIORIDAD = booleanPreferencesKey("orden_por_prioridad")
        val RECORDATORIOS_ACTIVADOS = booleanPreferencesKey("recordatorios_activados")
    }

    open val ordenarPorPrioridad: Flow<Boolean> = if (context != null) {
        context.dataStore.data.map { preferences ->
            preferences[ORDEN_POR_PRIORIDAD] ?: false
        }
    } else {
        flowOf(false)
    }

    open suspend fun guardarOrdenPorPrioridad(valor: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[ORDEN_POR_PRIORIDAD] = valor
        }
    }

    open val recordatoriosActivados: Flow<Boolean> = if (context != null) {
        context.dataStore.data.map { preferences ->
            preferences[RECORDATORIOS_ACTIVADOS] ?: false
        }
    } else {
        flowOf(false)
    }

    open suspend fun guardarRecordatoriosActivados(valor: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[RECORDATORIOS_ACTIVADOS] = valor
        }
    }
}
