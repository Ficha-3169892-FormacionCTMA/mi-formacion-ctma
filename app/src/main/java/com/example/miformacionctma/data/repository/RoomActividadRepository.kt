package com.example.miformacionctma.data.repository

import androidx.room3.useWriterConnection
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.data.mapper.toDto
import com.example.miformacionctma.data.mapper.toEntityList
import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.util.DataError
import com.example.miformacionctma.data.util.Result
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class RoomActividadRepository(
    private val dao: ActividadDao,
    private val competenciaDao: CompetenciaDao,
    private val db: FormacionDatabase,
    private val api: ActividadesApi
) : ActividadRepository {

    override fun observarActividades(): Flow<List<ActividadFormativa>> {
        return dao.observarTodas()
            .map { actividades ->
                actividades.map { it.toDomain() }
            }
    }

    override fun observarCompetencias(): Flow<List<Competencia>> {
        return competenciaDao.observarTodas()
            .map { competencias ->
                competencias.map { it.toDomain() }
            }
    }

    override suspend fun obtenerPorId(id: Long): ActividadFormativa? {
        return dao.obtenerPorId(id)?.toDomain()
    }

    override suspend fun insertar(actividad: ActividadFormativa) {
        try {
            val response = api.crearActividad(actividad.toDto())
            if (response.isSuccessful) {
                dao.insertar(actividad.toEntity())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    override suspend fun actualizar(actividad: ActividadFormativa) {
        try {
            val response = api.actualizarActividad("eq.${actividad.id}", actividad.toDto())
            if (response.isSuccessful) {
                dao.actualizar(actividad.toEntity())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    override suspend fun eliminar(actividad: ActividadFormativa) {
        try {
            val response = api.eliminarActividad("eq.${actividad.id}")
            if (response.isSuccessful) {
                dao.eliminar(actividad.toEntity())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    override suspend fun insertarConCompetencia(
        actividad: ActividadFormativa,
        competenciaId: Long?
    ) {
        dao.insertar(
            actividad.toEntity().copy(
                competenciaId = competenciaId
            )
        )
    }

    override fun observarPorTitulo(
        texto: String
    ): Flow<List<ActividadFormativa>> {
        return dao.buscarPorTitulo(texto)
            .map { actividades ->
                actividades.map { it.toDomain() }
            }
    }

    override suspend fun obtenerConCompetencia(
        id: Long
    ): Pair<ActividadFormativa, String?>? {
        val resultado = dao.obtenerConCompetencia(id) ?: return null

        return resultado.actividad.toDomain() to resultado.competencia?.nombre
    }

    override suspend fun inicializarDatos() {
        // En una arquitectura Offline-First conectada a Supabase,
        // no sembramos datos locales. La base de datos local se llenará
        // exclusivamente mediante la función refresh() cuando haya red.
        // Esto evita conflictos entre datos demo y datos reales del servidor.
    }

    override suspend fun refresh(): RepositoryResult<Unit> {
        return try {
            val actividadesResponse = api.getActividades()
            val competenciasResponse = api.getCompetencias()

            if (actividadesResponse.isSuccessful && competenciasResponse.isSuccessful) {
                val actividadesDto = actividadesResponse.body() ?: emptyList()
                val competenciasDto = competenciasResponse.body() ?: emptyList()
                
                val actividadesEntity = actividadesDto.toEntityList()
                val competenciasEntity = competenciasDto.toEntityList()

                // Transacción atómica en Room 3: borrar y reinsertar todo
                db.useWriterConnection {
                    dao.eliminarTodas()
                    dao.insertarTodas(actividadesEntity)
                    
                    competenciaDao.eliminarTodas()
                    competenciaDao.insertarTodas(competenciasEntity)
                }

                Result.Success(Unit)
            } else {
                val code = if (!actividadesResponse.isSuccessful) actividadesResponse.code() else competenciasResponse.code()
                val error = when (code) {
                    401 -> DataError.Network.Unauthorized
                    404 -> DataError.Network.NotFound
                    in 500..599 -> DataError.Network.Server
                    else -> DataError.Network.Unknown
                }
                Result.Error(error)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NoConnection)
        } catch (e: HttpException) {
            Result.Error(DataError.Network.Server)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error(DataError.Network.InvalidPayload)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Network.Unknown)
        }
    }
}
