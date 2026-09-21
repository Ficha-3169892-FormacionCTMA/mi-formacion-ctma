package com.example.miformacionctma.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.miformacionctma.data.local.EvidenciaDao
import com.example.miformacionctma.data.local.EvidenciaEntity
import com.example.miformacionctma.data.remote.EvidencePolicy
import com.example.miformacionctma.data.remote.EvidenciasApi
import com.example.miformacionctma.data.remote.classifyNetworkCall
import com.example.miformacionctma.data.remote.validateImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

interface EvidenciaRepository {
    fun observeEvidencias(actividadId: String): Flow<List<EvidenciaEntity>>
    suspend fun guardarEvidenciaLocal(actividadId: String, uri: Uri, resolver: ContentResolver): Result<String>
    suspend fun subirEvidencia(evidenciaId: String, resolver: ContentResolver): Result<Unit>
    suspend fun eliminarEvidencia(evidenciaId: String): Result<Unit>
}

class DefaultEvidenciaRepository(
    private val api: EvidenciasApi,
    private val dao: EvidenciaDao
) : EvidenciaRepository {

    override fun observeEvidencias(actividadId: String): Flow<List<EvidenciaEntity>> {
        return dao.observeEvidenciasForActividad(actividadId)
    }

    override suspend fun guardarEvidenciaLocal(
        actividadId: String,
        uri: Uri,
        resolver: ContentResolver
    ): Result<String> = runCatching {
        // 1. Validar la imagen antes de registrarla
        val metadata = validateImage(resolver, uri, EvidencePolicy()).getOrThrow()

        val evidenciaId = "EVI-${UUID.randomUUID()}"
        val nuevaEvidencia = EvidenciaEntity(
            id = evidenciaId,
            actividadId = actividadId,
            localUri = uri.toString(),
            mimeType = metadata.mimeType,
            sizeBytes = metadata.sizeBytes,
            estado = "LOCAL",
            creadaEnEpochMillis = System.currentTimeMillis()
        )

        dao.insertOrUpdate(nuevaEvidencia)
        evidenciaId
    }

    override suspend fun subirEvidencia(
        evidenciaId: String,
        resolver: ContentResolver
    ): Result<Unit> {
        val local = dao.findById(evidenciaId)
            ?: return Result.failure(Exception("Evidencia no encontrada en la base local"))

        dao.updateStatus(evidenciaId, "SUBIENDO")

        return classifyNetworkCall {
            val uri = Uri.parse(local.localUri)
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("No se pudo leer el archivo local")

            val requestFile = bytes.toRequestBody(local.mimeType.toMediaTypeOrNull())
            val bodyPart = MultipartBody.Part.createFormData("file", "evidencia.jpg", requestFile)

            val response = api.upload(
                actividadId = local.actividadId,
                evidenceId = local.id,
                file = bodyPart
            )

            if (response.isSuccessful) {
                dao.updateStatus(evidenciaId, "SINCRONIZADA")
            } else {
                dao.updateStatus(evidenciaId, "FALLIDA")
                throw Exception("Error del servidor: ${response.code()}")
            }
        }.onFailure { throwable ->
            if (throwable is CancellationException) {
                // Si el usuario canceló la corrutina, regresamos el estado a LOCAL
                dao.updateStatus(evidenciaId, "LOCAL")
                throw throwable
            } else {
                dao.updateStatus(evidenciaId, "FALLIDA")
            }
        }
    }

    override suspend fun eliminarEvidencia(evidenciaId: String): Result<Unit> = runCatching {
        val local = dao.findById(evidenciaId)
        if (local != null) {
            // Eliminar archivo físico si aplica
            val file = File(Uri.parse(local.localUri).path ?: "")
            if (file.exists()) file.delete()

            dao.deleteById(evidenciaId)
        }
    }
}