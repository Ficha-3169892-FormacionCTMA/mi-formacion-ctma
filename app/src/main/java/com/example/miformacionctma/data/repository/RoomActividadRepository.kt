package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActividadRepository(
    private val dao: ActividadDao
) : ActividadRepository {

    override fun observarActividades(): Flow<List<ActividadFormativa>> {
        return dao.observarTodas()
            .map { actividades ->
                actividades.map { it.toDomain() }
            }
    }

    override suspend fun obtenerPorId(id: Long): ActividadFormativa? {
        return dao.obtenerPorId(id)?.toDomain()
    }

    override suspend fun insertar(actividad: ActividadFormativa) {
        dao.insertar(actividad.toEntity())
    }

    override suspend fun actualizar(actividad: ActividadFormativa) {
        dao.actualizar(actividad.toEntity())
    }

    override suspend fun eliminar(actividad: ActividadFormativa) {
        dao.eliminar(actividad.toEntity())
    }

    override suspend fun inicializarDatos() {
        if (dao.contarActividades() == 0) {
            ActividadesDemo.listaInicial.forEach { actividad ->
                dao.insertar(actividad.toEntity())
            }
        }
    }
}
