package com.example.miformacionctma.data.repository

import android.content.Context
import android.net.Uri
import androidx.room3.useWriterConnection
import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entity.EstadoSincronizacion
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.data.mapper.toDto
import com.example.miformacionctma.data.mapper.toEntityList
import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.util.DataError
import com.example.miformacionctma.data.util.EvidenciaStorageUtil
import com.example.miformacionctma.data.util.Result
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.Instant

class RoomActividadRepository(
    private val dao: ActividadDao,
    private val competenciaDao: CompetenciaDao,
    private val evidenciaDao: EvidenciaDao,
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

                // Transacción atómica en Room 3:
                // 1. Eliminar localmente aquellas actividades que ya fueron borradas en el servidor (Supabase).
                // 2. Insertar o actualizar las actividades vigentes usando UPSERT.
                db.useWriterConnection {
                    val serverIds = actividadesEntity.map { it.id }
                    if (serverIds.isNotEmpty()) {
                        dao.eliminarActividadesNoPresentes(serverIds)
                    }
                    dao.insertarTodas(actividadesEntity)
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

    // Operaciones de Evidencias Fotográficas Múltiples (1 a muchos) con Aislamiento por Usuario o Vista Global de Instructor
    override fun observarEvidencias(actividadId: Long, usuarioId: String): Flow<List<EvidenciaEntity>> {
        return evidenciaDao.observarListaPorActividadYUsuario(actividadId, usuarioId)
    }

    override fun observarEvidenciasInstructor(actividadId: Long): Flow<List<EvidenciaEntity>> {
        return evidenciaDao.observarListaPorActividad(actividadId)
    }

    override suspend fun guardarEvidenciaLocal(
        actividadId: Long,
        localUri: String,
        mimeType: String,
        tamano: Long,
        usuarioId: String
    ): EvidenciaEntity {
        val evidencia = EvidenciaEntity(
            id = 0, // 0 fuerza la inserción de una nueva evidencia (relación 1 a muchos)
            actividadId = actividadId,
            localUri = localUri,
            mimeType = mimeType,
            tamano = tamano,
            fecha = Instant.now(),
            estado = EstadoSincronizacion.LOCAL,
            usuarioId = usuarioId
        )
        val idGenerado = evidenciaDao.insertar(evidencia)
        return evidencia.copy(id = idGenerado)
    }

    override suspend fun subirEvidencia(
        context: Context,
        evidenciaId: Long
    ): RepositoryResult<Unit> {
        val evidencia = evidenciaDao.obtenerPorId(evidenciaId)
            ?: return Result.Error(DataError.Network.Unknown)

        // 1. Cambiar estado a SUBIENDO
        evidenciaDao.actualizarEstado(evidencia.id, EstadoSincronizacion.SUBIENDO)

        return try {
            val uri = Uri.parse(evidencia.localUri)
            val inputStream = if (uri.scheme == "file" && uri.path != null) {
                FileInputStream(File(uri.path!!))
            } else {
                context.contentResolver.openInputStream(uri)
            } ?: throw IOException("No se pudo abrir el archivo de la evidencia")

            val bytes = inputStream.use { it.readBytes() }
            val mime = evidencia.mimeType.ifBlank { "image/jpeg" }
            val mediaType = mime.toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val nombreArchivo = "evidencia_${evidencia.actividadId}_${evidencia.id}_${evidencia.usuarioId.takeIf { it.isNotBlank() } ?: "general"}.jpg"

            // Construir la URL completa para Supabase Storage API
            val baseUrl = BuildConfig.SUPABASE_URL.replace("/rest/v1/", "/").trimEnd('/')
            val storageUrl = "$baseUrl/storage/v1/object/evidencias/$nombreArchivo"

            // 2. Intentar subida enviando el payload binario directo con Content-Type y x-upsert
            val response = api.subirEvidencia(
                url = storageUrl,
                idempotencyKey = evidencia.id.toString(),
                contentType = mime,
                body = requestBody
            )

            if (response.isSuccessful) {
                // 3. Éxito: cambiar a SINCRONIZADA
                evidenciaDao.actualizarEstado(evidencia.id, EstadoSincronizacion.SINCRONIZADA)
                Result.Success(Unit)
            } else {
                // 4. Error del servidor: cambiar a FALLIDA (mantiene archivo local)
                evidenciaDao.actualizarEstado(evidencia.id, EstadoSincronizacion.FALLIDA)
                Result.Error(DataError.Network.Server)
            }
        } catch (e: IOException) {
            // Error de red / timeout: cambiar a FALLIDA (mantiene archivo local)
            evidenciaDao.actualizarEstado(evidencia.id, EstadoSincronizacion.FALLIDA)
            Result.Error(DataError.Network.NoConnection)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            evidenciaDao.actualizarEstado(evidencia.id, EstadoSincronizacion.FALLIDA)
            Result.Error(DataError.Network.Unknown)
        }
    }

    override suspend fun eliminarEvidencia(
        context: Context,
        evidenciaId: Long
    ) {
        val evidencia = evidenciaDao.obtenerPorId(evidenciaId)
        if (evidencia != null) {
            EvidenciaStorageUtil.eliminarArchivoSiExiste(context, Uri.parse(evidencia.localUri))
            evidenciaDao.eliminar(evidencia)
        }
    }
}
