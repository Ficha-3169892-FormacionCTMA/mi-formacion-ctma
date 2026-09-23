package com.example.miformacionctma.data.repository

import android.util.Log
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.local.entity.toActividadFormativa
import com.example.miformacionctma.data.remote.ActividadRemotaDTO
import com.example.miformacionctma.data.remote.SupabaseApiService
import com.example.miformacionctma.data.remote.classifyNetworkCall
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

interface ActividadRepository {
    fun observeActividades(): Flow<List<ActividadFormativa>>
    suspend fun refresh(): Result<Unit>
    suspend fun insertActividad(actividad: ActividadFormativa)
    suspend fun updateActividad(actividad: ActividadFormativa)
    suspend fun deleteActividad(actividad: ActividadFormativa)

    // Métodos para gestionar evidencias
    fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?>
    suspend fun subirEvidencia(actividadId: Int, bytes: ByteArray, tipoMime: String): Result<Unit>
    suspend fun sincronizarEvidencias(): Result<Unit>
    suspend fun eliminarEvidencia(actividadId: Int)
}

/**
 * Actividades offline-first con respaldo en Supabase:
 * - Room es la fuente que ve la UI. Cada cambio local queda marcado como pendiente (sincronizada = false).
 * - refresh() envía los pendientes a Supabase y luego descarga las actividades que están en Supabase y no en el teléfono.
 */
class OfflineFirstActividadRepository(
    private val supabaseApiService: SupabaseApiService,
    private val dao: ActividadDao,
    private val evidenciaRepository: EvidenciaRepository
) : ActividadRepository {

    private val mutexSincronizacion = Mutex()

    override fun observeActividades(): Flow<List<ActividadFormativa>> =
        dao.obtenerTodasLasActividades().map { entities ->
            entities.map { it.toActividadFormativa() }
        }

    override suspend fun refresh(): Result<Unit> = mutexSincronizacion.withLock {
        classifyNetworkCall {
            enviarCambiosPendientes()
            descargarActividadesRemotas()
        }.onFailure { Log.w(TAG, "No se pudieron sincronizar las actividades", it) }
    }

    override suspend fun insertActividad(actividad: ActividadFormativa) {
        dao.insertarActividad(actividad.toEntity())
    }

    override suspend fun updateActividad(actividad: ActividadFormativa) {
        // Se conserva el estado 'completada' que no viene en el modelo de la UI
        val actual = dao.obtenerPorId(actividad.id.toInt())
        dao.actualizarActividad(actividad.toEntity(completada = actual?.completada ?: false))
    }

    override suspend fun deleteActividad(actividad: ActividadFormativa) {
        // Borra también la foto local y la de Supabase para que no quede huérfana
        evidenciaRepository.eliminar(actividad.id.toInt())
        // Borrado lógico: se oculta ya y se elimina de verdad cuando Supabase confirme (así funciona sin internet)
        val actual = dao.obtenerPorId(actividad.id.toInt()) ?: return
        dao.actualizarActividad(actual.copy(eliminada = true, sincronizada = false))
    }

    override fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?> =
        evidenciaRepository.observarEvidencia(actividadId)

    override suspend fun subirEvidencia(actividadId: Int, bytes: ByteArray, tipoMime: String): Result<Unit> =
        evidenciaRepository.guardarYSubir(actividadId, bytes, tipoMime)

    override suspend fun sincronizarEvidencias(): Result<Unit> =
        evidenciaRepository.sincronizar()

    override suspend fun eliminarEvidencia(actividadId: Int) =
        evidenciaRepository.eliminar(actividadId)

    private suspend fun enviarCambiosPendientes() {
        for (pendiente in dao.obtenerPendientes()) {
            if (pendiente.eliminada) {
                verificar(supabaseApiService.deleteActividad("eq.${pendiente.id}"), "No se pudo borrar la actividad en Supabase")
                dao.eliminarPorId(pendiente.id)
            } else {
                verificar(supabaseApiService.upsertActividad(pendiente.toRemota()), "No se pudo guardar la actividad en Supabase")
                // Si el usuario la editó mientras se enviaba, queda pendiente para la próxima sincronización
                if (dao.obtenerPorId(pendiente.id) == pendiente) dao.marcarSincronizada(pendiente.id)
            }
        }
    }

    private suspend fun descargarActividadesRemotas() {
        val respuesta = supabaseApiService.listActividades()
        verificar(respuesta, "No se pudo consultar la tabla actividades")

        for (remota in respuesta.body().orEmpty()) {
            val entidad = remota.toEntity()
            val local = dao.obtenerPorId(entidad.id)
            // Los cambios locales sin enviar tienen prioridad sobre lo que hay en Supabase
            if (local != null && (!local.sincronizada || local.eliminada)) continue
            // Upsert (no REPLACE) para no borrar en cascada las evidencias de la actividad
            if (local != entidad) dao.upsertActividad(entidad)
        }
    }

    private fun verificar(respuesta: Response<*>, mensaje: String) {
        if (!respuesta.isSuccessful) {
            val detalle = respuesta.errorBody()?.string().orEmpty()
            throw IllegalStateException("$mensaje (${respuesta.code()}) $detalle".trim())
        }
    }

    companion object {
        private const val TAG = "ActividadRepository"
    }
}

private fun ActividadFormativa.toEntity(completada: Boolean = false) = ActividadEntity(
    id = id.toInt(),
    titulo = titulo,
    descripcion = descripcion,
    progreso = progreso,
    prioridad = prioridad,
    fecha = fecha,
    completada = completada,
    sincronizada = false
)

private fun ActividadEntity.toRemota() = ActividadRemotaDTO(
    id = id.toLong(),
    titulo = titulo,
    descripcion = descripcion,
    fecha = fecha,
    prioridad = prioridad.name,
    progreso = progreso,
    completada = completada
)

private fun ActividadRemotaDTO.toEntity() = ActividadEntity(
    id = id.toInt(),
    titulo = titulo,
    descripcion = descripcion,
    fecha = fecha,
    prioridad = runCatching { Prioridad.valueOf(prioridad.uppercase()) }.getOrDefault(Prioridad.MEDIA),
    progreso = progreso.coerceIn(0, 100),
    completada = completada,
    sincronizada = true
)
