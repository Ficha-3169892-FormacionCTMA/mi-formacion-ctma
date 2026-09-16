package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.remote.ActividadesApi
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.remote.classifyNetworkCall
import com.example.miformacionctma.data.remote.model.toEntity
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

interface ActividadRepository {
    fun observeActividades(): Flow<List<ActividadFormativa>>
    suspend fun refresh(): Result<Unit>
    suspend fun insertActividad(actividad: ActividadFormativa)
    suspend fun updateActividad(actividad: ActividadFormativa)
    suspend fun deleteActividad(actividad: ActividadFormativa)
}

class OfflineFirstActividadRepository(
    private val api: ActividadesApi,
    private val dao: ActividadDao
) : ActividadRepository {

    override fun observeActividades(): Flow<List<ActividadFormativa>> =
        dao.obtenerTodasLasActividades().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refresh(): Result<Unit> = classifyNetworkCall {
        val response = api.listar()

        when {
            response.code() == 401 -> throw NetworkFailure(DataError.Unauthorized)
            response.code() in 500..599 -> throw NetworkFailure(DataError.Server(response.code()))
            !response.isSuccessful -> throw NetworkFailure(DataError.Unknown(HttpException(response)))
        }

        val dtos = response.body().orEmpty()
        val entities = dtos.map { it.toEntity() }

        entities.forEach { dao.insertarActividad(it) }
    }

    override suspend fun insertActividad(actividad: ActividadFormativa) {
        val entity = ActividadEntity(
            id = actividad.id.toInt(),
            titulo = actividad.titulo,
            descripcion = actividad.descripcion,
            progreso = actividad.progreso,
            prioridad = actividad.prioridad,
            fecha = actividad.fecha
        )
        dao.insertarActividad(entity)
    }

    override suspend fun updateActividad(actividad: ActividadFormativa) {
        val entity = ActividadEntity(
            id = actividad.id.toInt(),
            titulo = actividad.titulo,
            descripcion = actividad.descripcion,
            progreso = actividad.progreso,
            prioridad = actividad.prioridad,
            fecha = actividad.fecha
        )
        dao.actualizarActividad(entity)
    }

    override suspend fun deleteActividad(actividad: ActividadFormativa) {
        val entity = ActividadEntity(
            id = actividad.id.toInt(),
            titulo = actividad.titulo,
            descripcion = actividad.descripcion,
            progreso = actividad.progreso,
            prioridad = actividad.prioridad,
            fecha = actividad.fecha
        )
        dao.eliminarActividad(entity)
    }
}

// Fuera de la clase para actuar como función de extensión global del paquete
fun ActividadEntity.toDomain(): ActividadFormativa {
    return ActividadFormativa(
        id = id.toLong(),
        titulo = titulo,
        descripcion = descripcion,
        progreso = progreso,
        prioridad = prioridad,
        fecha = fecha,
        diasRestantes = 0
    )
}