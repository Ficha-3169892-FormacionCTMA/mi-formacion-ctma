package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.remote.ActividadesApi
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.remote.classifyNetworkCall
import com.example.miformacionctma.data.remote.model.toEntity
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

interface ActividadRepository {
    fun observeActividades(): Flow<List<ActividadFormativa>>
    suspend fun refresh(): Result<Unit>
    suspend fun insertActividad(actividad: ActividadFormativa)
    suspend fun updateActividad(actividad: ActividadFormativa)
    suspend fun deleteActividad(actividad: ActividadFormativa)

    // Métodos para gestionar evidencias
    fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?>
    suspend fun guardarEvidenciaLocal(actividadId: Int, uri: String, tipo: String, tamano: Long)
    suspend fun subirEvidenciaAlServidor(actividadId: Int, bytes: ByteArray, tipoMime: String, nombreArchivo: String): Result<Unit>
    suspend fun eliminarEvidencia(actividadId: Int)
}

class OfflineFirstActividadRepository(
    private val api: ActividadesApi,
    private val dao: ActividadDao,
    private val evidenciaDao: EvidenciaDao
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

    override fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?> {
        return evidenciaDao.observarEvidenciaPorActividad(actividadId)
    }

    override suspend fun guardarEvidenciaLocal(actividadId: Int, uri: String, tipo: String, tamano: Long) {
        val evidencia = EvidenciaEntity(
            actividadId = actividadId,
            uri = uri,
            tipo = tipo,
            tamano = tamano,
            estado = "LOCAL"
        )
        evidenciaDao.insertarEvidencia(evidencia)
    }

    override suspend fun subirEvidenciaAlServidor(actividadId: Int, bytes: ByteArray, tipoMime: String, nombreArchivo: String): Result<Unit> {
        val evidenciaActual = evidenciaDao.obtenerEvidenciaPorActividad(actividadId)
            ?: return Result.failure(Exception("No existe evidencia local"))

        evidenciaDao.insertarEvidencia(evidenciaActual.copy(estado = "SUBIENDO"))

        return try {
            val mediaType = tipoMime.toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("file", nombreArchivo, requestBody)

            val response = api.subirEvidencia(actividadId.toString(), part)
            if (response.isSuccessful) {
                evidenciaDao.insertarEvidencia(evidenciaActual.copy(estado = "SINCRONIZADA"))
                Result.success(Unit)
            } else {
                evidenciaDao.insertarEvidencia(evidenciaActual.copy(estado = "FALLIDA"))
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            evidenciaDao.insertarEvidencia(evidenciaActual.copy(estado = "FALLIDA"))
            Result.failure(e)
        }
    }

    override suspend fun eliminarEvidencia(actividadId: Int) {
        evidenciaDao.eliminarEvidenciaPorActividad(actividadId)
    }
}

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
