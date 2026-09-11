package com.example.miformacionctma.data.repository

import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow

interface ActividadRepository {

    fun observarActividades(): Flow<List<ActividadFormativa>>

    fun buscar(texto: String): Flow<List<ActividadFormativa>>

    suspend fun guardar(actividad: ActividadFormativa)

    suspend fun eliminar(id: Long)

    suspend fun refresh()
}
