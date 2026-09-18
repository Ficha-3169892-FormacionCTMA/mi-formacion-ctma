package com.example.miformacionctma.data.repository

import android.net.Uri
import com.example.miformacionctma.model.Evidencia
import kotlinx.coroutines.flow.Flow

interface EvidenciaRepository {

    fun observarEvidencias(actividadId: Long): Flow<List<Evidencia>>

    suspend fun guardarEvidenciaLocal(
        actividadId: Long,
        uri: Uri,
        finalidad: String?
    ): Evidencia

    suspend fun sincronizarEvidencia(evidenciaId: Long)

    suspend fun eliminarEvidencia(evidenciaId: Long)

    suspend fun obtenerEvidencia(evidenciaId: Long): Evidencia?

    suspend fun refrescarDesdeServidor(actividadId: Long)
}
