package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.ActividadEntity
import kotlinx.coroutines.flow.Flow

class ActividadRepository(
    private val actividadDao: ActividadDao
) {
    val todasLasActividades: Flow<List<ActividadEntity>> = actividadDao.obtenerTodasLasActividades()

    suspend fun obtenerPorId(id: Int): ActividadEntity? {
        return actividadDao.obtenerActividadPorId(id)
    }

    suspend fun insertar(actividad: ActividadEntity) {
        actividadDao.insertarActividad(actividad)
    }

    suspend fun actualizar(actividad: ActividadEntity) {
        actividadDao.actualizarActividad(actividad)
    }

    suspend fun eliminar(actividad: ActividadEntity) {
        actividadDao.eliminarActividad(actividad)
    }
}