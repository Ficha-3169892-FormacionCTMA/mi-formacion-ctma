package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.miformacionctma.data.local.PreferenciasUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_usuario")

class PreferenciasRepository(private val context: Context) {

    private object Keys {
        val FILTRO = stringPreferencesKey("filtro_competencia")
        val ORDEN_DESC = booleanPreferencesKey("orden_desc")
        val MODO_CUADRICULA = booleanPreferencesKey("modo_cuadricula")
    }

    val preferencias: Flow<PreferenciasUsuario> = context.dataStore.data.map { p: Preferences ->
        PreferenciasUsuario(
            filtroCompetencia = p[Keys.FILTRO],
            ordenDescendente = p[Keys.ORDEN_DESC] ?: true,
            modoCuadricula = p[Keys.MODO_CUADRICULA] ?: false
        )
    }

    suspend fun guardarFiltro(valor: String?) {
        context.dataStore.edit { p: Preferences ->
            if (valor == null) {
                p.remove(Keys.FILTRO)
            } else {
                p[Keys.FILTRO] = valor
            }
        }
    }

    suspend fun guardarModoCuadricula(activo: Boolean) {
        context.dataStore.edit { p: Preferences ->
            p[Keys.MODO_CUADRICULA] = activo
        }
    }
}