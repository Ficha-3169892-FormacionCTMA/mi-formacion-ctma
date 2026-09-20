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

    val accessToken: Flow<String?>
    val userId: Flow<String?>
    val userRole: Flow<String?>

    suspend fun guardarSesion(token: String, id: String, rol: String)
    suspend fun borrarSesion()
}

class PreferenciasRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferenciasRepository {

    private companion object {
        val ORDEN = stringPreferencesKey("orden")
        val TOKEN = stringPreferencesKey("access_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
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

    override val accessToken: Flow<String?> = dataStore.data.map { it[TOKEN] }
    override val userId: Flow<String?> = dataStore.data.map { it[USER_ID] }
    override val userRole: Flow<String?> = dataStore.data.map { it[USER_ROLE] }

    override suspend fun guardarSesion(token: String, id: String, rol: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[USER_ID] = id
            preferences[USER_ROLE] = rol
        }
    }

    override suspend fun borrarSesion() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USER_ROLE)
        }
    }
}
