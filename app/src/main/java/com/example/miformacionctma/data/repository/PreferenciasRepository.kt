package com.example.miformacionctma.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.miformacionctma.model.RolUsuario
import com.example.miformacionctma.model.Usuario
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
        
        val USUARIO_ID = stringPreferencesKey("usuario_id")
        val USUARIO_EMAIL = stringPreferencesKey("usuario_email")
        val USUARIO_NOMBRE = stringPreferencesKey("usuario_nombre")
        val USUARIO_ROL = stringPreferencesKey("usuario_rol")
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

    open val usuarioSesion: Flow<Usuario?> = if (context != null) {
        context.dataStore.data.map { preferences ->
            val id = preferences[USUARIO_ID]
            val email = preferences[USUARIO_EMAIL]
            val nombre = preferences[USUARIO_NOMBRE]
            val rolString = preferences[USUARIO_ROL]

            if (!id.isNullOrEmpty() && !email.isNullOrEmpty() && !nombre.isNullOrEmpty() && !rolString.isNullOrEmpty()) {
                val rol = try {
                    RolUsuario.valueOf(rolString)
                } catch (e: Exception) {
                    RolUsuario.APRENDIZ
                }
                Usuario(id = id, email = email, nombre = nombre, rol = rol)
            } else {
                null
            }
        }
    } else {
        flowOf(null)
    }

    open suspend fun guardarSesionUsuario(usuario: Usuario) {
        context?.dataStore?.edit { preferences ->
            preferences[USUARIO_ID] = usuario.id
            preferences[USUARIO_EMAIL] = usuario.email
            preferences[USUARIO_NOMBRE] = usuario.nombre
            preferences[USUARIO_ROL] = usuario.rol.name
        }
    }

    open suspend fun cerrarSesionUsuario() {
        context?.dataStore?.edit { preferences ->
            preferences.remove(USUARIO_ID)
            preferences.remove(USUARIO_EMAIL)
            preferences.remove(USUARIO_NOMBRE)
            preferences.remove(USUARIO_ROL)
        }
    }
}
