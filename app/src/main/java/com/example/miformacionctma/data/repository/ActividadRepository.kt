package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.data.remote.ActividadesApi
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.remote.classifyNetworkCall
import com.example.miformacionctma.data.remote.toEntity
import kotlinx.coroutines.flow.Flow

interface ActividadRepository {
    fun observeActividades(): Flow<List<ActividadEntity>>
    suspend fun refresh(): Result<Unit>
}

class OfflineFirstActividadRepository(
    private val api: ActividadesApi,
    private val dao: ActividadDao
) : ActividadRepository {

    override fun observeActividades(): Flow<List<ActividadEntity>> {
        return dao.observarTodos() // Actualizado al nombre correcto del DAO
    }

    override suspend fun refresh(): Result<Unit> = classifyNetworkCall {
        val response = api.listar()

        when {
            response.code() == 401 -> throw NetworkFailure(DataError.Unauthorized)
            response.code() == 404 -> throw NetworkFailure(DataError.NotFound)
            response.code() in 500..599 -> throw NetworkFailure(DataError.Server(response.code()))
            !response.isSuccessful -> throw NetworkFailure(DataError.Unknown(Exception("HTTP ${response.code()}")))
        }

        val dtoList = response.body().orEmpty()
        val entities = dtoList.map { it.toEntity() }

        // Sincronización en Room usando la inserción masiva
        dao.insertarTodas(entities)
    }
}