package com.example.miformacionctma.data.repository

import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow

interface ActividadRepository {

    fun observarActividades(): Flow<List<ActividadFormativa>>

    suspend fun obtenerPorId(id: Long): ActividadFormativa?

    suspend fun insertar(actividad: ActividadFormativa)

    suspend fun actualizar(actividad: ActividadFormativa)

    suspend fun eliminar(actividad: ActividadFormativa)

    suspend fun inicializarDatos()
}
