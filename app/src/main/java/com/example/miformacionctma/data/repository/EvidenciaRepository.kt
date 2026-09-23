package com.example.miformacionctma.data.repository

import android.util.Log
import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity.Companion.ESTADO_FALLIDA
import com.example.miformacionctma.data.local.entity.EvidenciaEntity.Companion.ESTADO_PENDIENTE
import com.example.miformacionctma.data.local.entity.EvidenciaEntity.Companion.ESTADO_SINCRONIZADA
import com.example.miformacionctma.data.remote.EvidenciaDTO
import com.example.miformacionctma.data.remote.EvidenciaRemotaDTO
import com.example.miformacionctma.data.remote.SupabaseApiService
import com.example.miformacionctma.data.remote.SupabaseConfig
import com.example.miformacionctma.data.remote.classifyNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

/**
 * Evidencias offline-first:
 * - La imagen se guarda primero en el almacenamiento interno de la app y en Room (no se pierde al cerrar la app).
 * - Luego se sube a Supabase Storage y se registra en la tabla 'evidencias'.
 * - Al sincronizar se reintentan las subidas pendientes y se descargan las fotos que están en Supabase y no en el teléfono.
 */
class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao,
    private val actividadDao: ActividadDao,
    private val supabaseApiService: SupabaseApiService,
    private val directorioEvidencias: File
) {

    private val mutexSincronizacion = Mutex()

    fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?> =
        evidenciaDao.observarEvidenciaPorActividad(actividadId)

    suspend fun guardarYSubir(actividadId: Int, bytes: ByteArray, tipoMime: String): Result<Unit> {
        val archivo = withContext(Dispatchers.IO) {
            directorioEvidencias.mkdirs()
            File(directorioEvidencias, "evidencia_${actividadId}_${System.currentTimeMillis()}.jpg")
                .apply { writeBytes(bytes) }
        }

        // 1. Guardar en Room antes de subir, así la foto queda aunque no haya internet
        val anterior = evidenciaDao.obtenerEvidenciaPorActividad(actividadId)
        val nueva = EvidenciaEntity(
            actividadId = actividadId,
            uri = archivo.absolutePath,
            tipo = tipoMime,
            tamano = archivo.length(),
            estado = ESTADO_PENDIENTE
        )
        evidenciaDao.insertarEvidencia(nueva)

        // 2. Si reemplaza a una foto anterior, se borra la anterior (local y remota)
        if (anterior != null) {
            borrarArchivoLocal(anterior.uri)
            eliminarRemota(actividadId)
        }

        // 3. Subir a Supabase
        return subir(nueva)
    }

    suspend fun eliminar(actividadId: Int) {
        val evidencia = evidenciaDao.obtenerEvidenciaPorActividad(actividadId)
        evidenciaDao.eliminarEvidenciaPorActividad(actividadId)
        evidencia?.let { borrarArchivoLocal(it.uri) }
        eliminarRemota(actividadId)
    }

    suspend fun sincronizar(): Result<Unit> = mutexSincronizacion.withLock {
        // 1. Reintentar las que no se pudieron subir (sin internet, error del servidor, etc.)
        evidenciaDao.obtenerTodas()
            .filter { it.estado != ESTADO_SINCRONIZADA }
            .forEach { subir(it) }

        // 2. Descargar las que están en Supabase y faltan en el teléfono
        classifyNetworkCall {
            val respuesta = supabaseApiService.listEvidenciaData()
            verificar(respuesta, "Error al consultar la tabla evidencias")

            val masRecientePorActividad = respuesta.body().orEmpty()
                .groupBy { it.actividadId.content }
                .mapValues { (_, filas) -> filas.maxBy { marcaDeTiempo(it.urlRemota) } }

            for ((idTexto, remota) in masRecientePorActividad) {
                val actividadId = idTexto.toIntOrNull() ?: continue
                // Room exige que la actividad exista (llave foránea)
                if (!actividadDao.existeActividad(actividadId)) continue

                val local = evidenciaDao.obtenerEvidenciaPorActividad(actividadId)
                // Hay un cambio local que todavía no se ha subido: tiene prioridad sobre el remoto
                if (local != null && local.estado != ESTADO_SINCRONIZADA) continue
                if (local != null && local.urlRemota == remota.urlRemota && File(local.uri).exists()) continue

                try {
                    descargar(actividadId, remota, local)
                } catch (e: Exception) {
                    Log.e(TAG, "No se pudo descargar la evidencia de la actividad $actividadId", e)
                }
            }
        }
    }

    private suspend fun subir(evidencia: EvidenciaEntity): Result<Unit> {
        val archivo = File(evidencia.uri)
        if (!archivo.exists()) {
            evidenciaDao.insertarEvidencia(evidencia.copy(estado = ESTADO_FALLIDA))
            return Result.failure(IllegalStateException("La imagen ya no existe en el teléfono"))
        }

        val resultado = classifyNetworkCall {
            val cuerpo = archivo.asRequestBody(evidencia.tipo.toMediaTypeOrNull())
            verificar(
                supabaseApiService.uploadEvidenciaFile(archivo.name, cuerpo),
                "Supabase Storage rechazó la imagen"
            )

            val urlRemota = urlPublica(archivo.name)
            verificar(
                supabaseApiService.insertEvidenciaData(
                    EvidenciaDTO(
                        actividad_id = evidencia.actividadId.toString(),
                        url_remota = urlRemota,
                        tipo_mime = evidencia.tipo,
                        tamano_bytes = archivo.length()
                    )
                ),
                "Supabase rechazó el registro en la tabla evidencias"
            )
            urlRemota
        }

        // Solo se actualiza si la evidencia sigue existiendo (el usuario pudo borrarla mientras subía)
        val actual = evidenciaDao.obtenerEvidenciaPorActividad(evidencia.actividadId)
        if (actual != null && actual.uri == evidencia.uri) {
            resultado
                .onSuccess { url ->
                    evidenciaDao.insertarEvidencia(actual.copy(estado = ESTADO_SINCRONIZADA, urlRemota = url))
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló la subida de la evidencia ${evidencia.actividadId}", error)
                    evidenciaDao.insertarEvidencia(actual.copy(estado = ESTADO_FALLIDA))
                }
        }
        return resultado.map { }
    }

    private suspend fun descargar(actividadId: Int, remota: EvidenciaRemotaDTO, local: EvidenciaEntity?) {
        val respuesta = supabaseApiService.downloadEvidenciaFile(remota.urlRemota)
        verificar(respuesta, "No se pudo descargar la imagen")

        val nombre = remota.urlRemota.substringAfterLast('/').ifBlank {
            "evidencia_${actividadId}_${System.currentTimeMillis()}.jpg"
        }
        val archivo = withContext(Dispatchers.IO) {
            directorioEvidencias.mkdirs()
            File(directorioEvidencias, nombre).apply {
                respuesta.body()!!.byteStream().use { entrada -> outputStream().use { entrada.copyTo(it) } }
            }
        }

        evidenciaDao.insertarEvidencia(
            EvidenciaEntity(
                actividadId = actividadId,
                uri = archivo.absolutePath,
                tipo = remota.tipoMime,
                tamano = archivo.length(),
                estado = ESTADO_SINCRONIZADA,
                urlRemota = remota.urlRemota
            )
        )
        if (local != null && local.uri != archivo.absolutePath) borrarArchivoLocal(local.uri)
    }

    // Borra el archivo y el registro en Supabase. Si falla (sin internet) no bloquea la operación local
    private suspend fun eliminarRemota(actividadId: Int) {
        classifyNetworkCall {
            val filas = supabaseApiService.listEvidenciaData(actividadId = "eq.$actividadId").body().orEmpty()
            filas.forEach { supabaseApiService.deleteEvidenciaFile(it.urlRemota.substringAfterLast('/')) }
            supabaseApiService.deleteEvidenciaData("eq.$actividadId")
        }.onFailure { Log.w(TAG, "No se pudo borrar la evidencia remota de $actividadId", it) }
    }

    private suspend fun borrarArchivoLocal(ruta: String) = withContext(Dispatchers.IO) {
        val archivo = File(ruta)
        // Solo se borran archivos dentro de la carpeta de evidencias de la app
        if (archivo.parentFile == directorioEvidencias) archivo.delete()
    }

    private fun verificar(respuesta: Response<*>, mensaje: String) {
        if (!respuesta.isSuccessful) {
            val detalle = respuesta.errorBody()?.string().orEmpty()
            throw IllegalStateException("$mensaje (${respuesta.code()}) $detalle".trim())
        }
    }

    private fun urlPublica(nombreArchivo: String) =
        "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/public/${SupabaseConfig.BUCKET}/$nombreArchivo"

    // Los nombres de archivo llevan System.currentTimeMillis(), sirve para elegir la foto más reciente
    private fun marcaDeTiempo(url: String): Long =
        Regex("""\d{13}""").findAll(url).lastOrNull()?.value?.toLongOrNull() ?: 0L

    companion object {
        private const val TAG = "EvidenciaRepository"
    }
}
