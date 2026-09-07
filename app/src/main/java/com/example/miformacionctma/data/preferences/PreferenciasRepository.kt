package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val ORDEN = stringPreferencesKey("orden")
    }

    val orden: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[ORDEN] ?: "id"
        }

    suspend fun guardarOrden(orden: String) {
        context.dataStore.edit { preferences ->
            preferences[ORDEN] = orden
        }
    }
}