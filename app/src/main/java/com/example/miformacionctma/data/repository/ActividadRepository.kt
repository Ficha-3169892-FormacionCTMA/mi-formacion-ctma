package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

class ActividadRepository(
    private val actividadDao: ActividadDao
) {
    fun obtenerTodasLasActividades(): Flow<List<ActividadEntity>> {
        return actividadDao.obtenerTodasLasActividades()
    }

    suspend fun insertarActividad(actividad: ActividadEntity) {
        actividadDao.insertarActividad(actividad)
    }

    suspend fun actualizarActividad(actividad: ActividadEntity) {
        actividadDao.actualizarActividad(actividad)
    }

    suspend fun eliminarActividad(actividad: ActividadEntity) {
        actividadDao.eliminarActividad(actividad)
    }
}