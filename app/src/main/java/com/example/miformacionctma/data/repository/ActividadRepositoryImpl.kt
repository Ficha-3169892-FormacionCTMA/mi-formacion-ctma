package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import com.example.miformacionctma.data.remote.toEntity
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ActividadRepositoryImpl(
    private val dao: ActividadDao,
    private val remoteDataSource: RemoteActividadDataSource
) : ActividadRepository {

    private val refreshMutex = Mutex()

    override fun observarActividades(): Flow<List<ActividadFormativa>> {
        return dao.observarTodas()
            .map { lista ->
                lista.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun buscar(texto: String): Flow<List<ActividadFormativa>> {
        return dao.buscar(texto.trim())
            .map { lista ->
                lista.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun guardar(actividad: ActividadFormativa) {
        dao.guardar(actividad.toEntity())
    }

    override suspend fun eliminar(id: Long) {
        dao.eliminar(id)
    }

    override suspend fun refresh() {
        refreshMutex.withLock {
            val remoteData = remoteDataSource.getActividades()
            val entities = remoteData.map { it.toEntity() }
            dao.refrescarActividades(entities)
        }
    }
}
