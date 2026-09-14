package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "preferencias")

data class PreferenciasUsuario(
    val filtroCompetencia: String?,
    val ordenDescendente: Boolean,
    val modoCuadricula: Boolean
)

open class PreferenciasRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val filtro = stringPreferencesKey("filtro_competencia")
        val ordenDesc = booleanPreferencesKey("orden_desc")
        val modoCuadricula = booleanPreferencesKey("modo_cuadricula")
    }

    open val preferencias: Flow<PreferenciasUsuario> = dataStore.data.map { p ->
        PreferenciasUsuario(
            filtroCompetencia = p[Keys.filtro],
            ordenDescendente = p[Keys.ordenDesc] ?: true,
            modoCuadricula = p[Keys.modoCuadricula] ?: false
        )
    }

    suspend fun guardarFiltro(valor: String?) {
        dataStore.edit { p ->
            if (valor == null) p.remove(Keys.filtro) else p[Keys.filtro] = valor
        }
    }

    suspend fun guardarModoCuadricula(activo: Boolean) {
        dataStore.edit { p -> p[Keys.modoCuadricula] = activo }
    }
}
