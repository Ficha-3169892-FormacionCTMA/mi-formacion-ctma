package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(
    name = "preferencias"
)

interface PreferenciasRepository {
    val orden: Flow<String>
    suspend fun guardarOrden(orden: String)
}

class PreferenciasRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferenciasRepository {

    private companion object {
        val ORDEN = stringPreferencesKey("orden")
    }

    override val orden: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[ORDEN] ?: "id"
        }

    override suspend fun guardarOrden(orden: String) {
        dataStore.edit { preferences ->
            preferences[ORDEN] = orden
        }
    }
}