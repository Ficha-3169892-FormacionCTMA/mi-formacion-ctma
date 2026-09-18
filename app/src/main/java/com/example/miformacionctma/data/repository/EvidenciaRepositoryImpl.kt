package com.example.miformacionctma.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.miformacionctma.data.local.EvidenciaDao
import com.example.miformacionctma.data.local.EvidenciaEntity
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.data.remote.RemoteEvidenciaDataSource
import com.example.miformacionctma.data.remote.RetrofitInstance
import com.example.miformacionctma.model.EstadoEvidencia
import com.example.miformacionctma.model.Evidencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenciaRepositoryImpl(
    private val context: Context,
    private val dao: EvidenciaDao,
    private val remote: RemoteEvidenciaDataSource
) : EvidenciaRepository {

    private val bucketName = "evidencias"
    private val maxSizeBytes = 5 * 1024 * 1024 // 5MB

    override fun observarEvidencias(actividadId: Long): Flow<List<Evidencia>> {
        return dao.observarPorActividad(actividadId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun guardarEvidenciaLocal(
        actividadId: Long,
        uri: Uri,
        finalidad: String?
    ): Evidencia = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        
        if (!mimeType.startsWith("image/")) {
            throw Exception("El archivo seleccionado no es una imagen válida.")
        }

        val metadata = obtenerMetadata(uri)
        if (metadata.tamanio > maxSizeBytes) {
            throw Exception("La imagen excede el tamaño máximo permitido (5MB).")
        }

        val nombreArchivo = generarNombreArchivo(mimeType)
        val fileLocal = File(obtenerDirectorioEvidencias(), nombreArchivo)
        
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(fileLocal).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("No se pudo leer la imagen seleccionada.")

        val evidencia = Evidencia(
            actividadId = actividadId,
            uriLocal = fileLocal.absolutePath,
            nombreArchivo = nombreArchivo,
            mimeType = mimeType,
            tamanio = metadata.tamanio,
            fechaCreacion = System.currentTimeMillis(),
            estado = EstadoEvidencia.LOCAL,
            finalidad = finalidad
        )

        val id = dao.guardar(evidencia.toEntity())
        evidencia.copy(id = id)
    }

    override suspend fun sincronizarEvidencia(evidenciaId: Long) = withContext(Dispatchers.IO) {
        val entity = dao.obtenerPorId(evidenciaId) ?: return@withContext
        val evidencia = entity.toDomain()

        try {
            dao.actualizarEstado(evidenciaId, EstadoEvidencia.SUBIENDO.name)
            
            val file = File(evidencia.uriLocal)
            if (!file.exists()) {
                throw Exception("El archivo local no existe.")
            }
            
            val bytes = file.readBytes()
            val remotePath = "${evidencia.actividadId}/${evidencia.nombreArchivo}"
            
            // URL pública en Supabase
            val projectUrl = RetrofitInstance.PROJECT_URL
            val remoteUrl = "${projectUrl}storage/v1/object/public/$bucketName/$remotePath"

            remote.subirEvidencia(
                bucket = bucketName,
                path = remotePath,
                bytes = bytes,
                mimeType = evidencia.mimeType,
                actividadId = evidencia.actividadId,
                nombreArchivo = evidencia.nombreArchivo,
                remoteUrl = remoteUrl
            )
            
            val entityActualizada = entity.copy(
                estado = EstadoEvidencia.SINCRONIZADA.name,
                remoteUrl = remoteUrl
            )
            dao.guardar(entityActualizada)

        } catch (e: Exception) {
            dao.actualizarEstado(evidenciaId, EstadoEvidencia.FALLIDA.name)
            throw e
        }
    }

    override suspend fun eliminarEvidencia(evidenciaId: Long) = withContext(Dispatchers.IO) {
        val entity = dao.obtenerPorId(evidenciaId) ?: return@withContext
        val evidencia = entity.toDomain()

        // Eliminar archivo local
        val file = File(evidencia.uriLocal)
        if (file.exists()) {
            file.delete()
        }

        // Intentar eliminar remoto (opcional, no bloqueante para el borrado local)
        try {
            val remotePath = "${evidencia.actividadId}/${evidencia.nombreArchivo}"
            remote.eliminarEvidencia(
                bucket = bucketName,
                path = remotePath,
                nombreArchivo = evidencia.nombreArchivo
            )
        } catch (e: Exception) {
            // Log error but continue
        }

        dao.eliminar(entity)
    }

    override suspend fun obtenerEvidencia(evidenciaId: Long): Evidencia? = withContext(Dispatchers.IO) {
        dao.obtenerPorId(evidenciaId)?.toDomain()
    }

    override suspend fun refrescarDesdeServidor(actividadId: Long) = withContext(Dispatchers.IO) {
        val remotas = remote.obtenerEvidencias(actividadId)
        
        remotas.forEach { dto ->
            val evidencia = Evidencia(
                actividadId = dto.actividadId,
                uriLocal = "", // Se cargará vía remoteUrl
                nombreArchivo = dto.nombreArchivo,
                mimeType = dto.mimeType,
                tamanio = 0,
                fechaCreacion = System.currentTimeMillis(),
                estado = EstadoEvidencia.SINCRONIZADA,
                remoteUrl = dto.remoteUrl
            )
            dao.guardar(evidencia.toEntity())
        }
    }

    private fun obtenerDirectorioEvidencias(): File {
        val dir = File(context.filesDir, "evidencias")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun generarNombreArchivo(mimeType: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = if (mimeType == "image/png") "png" else "jpg"
        return "EVI_${timestamp}.${extension}"
    }

    private data class FileMetadata(val nombre: String, val tamanio: Long)

    private fun obtenerMetadata(uri: Uri): FileMetadata {
        var nombre = "archivo"
        var tamanio = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                nombre = if (nameIndex != -1) cursor.getString(nameIndex) else nombre
                tamanio = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
            }
        }
        return FileMetadata(nombre, tamanio)
    }
}
