package com.example.miformacionctma.domain.repository

import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow

interface ActividadRepository {
    fun observarTodos(): Flow<List<ActividadFormativa>>
    fun buscar(texto: String): Flow<List<ActividadFormativa>>
    suspend fun guardar(actividad: ActividadFormativa)
    suspend fun eliminar(id: String): Boolean
}
