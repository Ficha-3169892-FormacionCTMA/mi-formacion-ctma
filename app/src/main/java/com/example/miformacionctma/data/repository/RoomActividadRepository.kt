package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.remote.toDomain
import com.example.miformacionctma.data.remote.toEntity
import com.example.miformacionctma.domain.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActividadRepository(
    private val dao: ActividadDao
) : ActividadRepository {
    override fun observarTodos(): Flow<List<ActividadFormativa>> =
        dao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    override fun buscar(texto: String): Flow<List<ActividadFormativa>> =
        dao.buscar(texto).map { lista -> lista.map { it.toDomain() } }

    override suspend fun guardar(actividad: ActividadFormativa) {
        dao.insertar(actividad.toEntity())
    }

    override suspend fun eliminar(id: String): Boolean =
        dao.eliminarPorId(id) == 1
}