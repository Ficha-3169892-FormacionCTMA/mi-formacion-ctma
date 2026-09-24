package com.example.miformacionctma.data.repository

import android.content.Context
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.util.DataError
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import kotlinx.coroutines.flow.Flow

/**
 * Resultado genérico para operaciones de datos.
 */
typealias RepositoryResult<T> = com.example.miformacionctma.data.util.Result<T, DataError.Network>

interface ActividadRepository {

    fun observarActividades(): Flow<List<ActividadFormativa>>

    fun observarCompetencias(): Flow<List<Competencia>>

    suspend fun obtenerPorId(id: Long): ActividadFormativa?

    suspend fun insertar(actividad: ActividadFormativa)

    suspend fun actualizar(actividad: ActividadFormativa)

    suspend fun eliminar(actividad: ActividadFormativa)

    suspend fun insertarConCompetencia(
        actividad: ActividadFormativa,
        competenciaId: Long?
    )

    fun observarPorTitulo(texto: String): Flow<List<ActividadFormativa>>

    suspend fun obtenerConCompetencia(
        id: Long
    ): Pair<ActividadFormativa, String?>?

    suspend fun inicializarDatos()

    /**
     * Sincroniza los datos remotos con la base de datos local.
     */
    suspend fun refresh(): RepositoryResult<Unit>

    // Operaciones de Evidencia Fotográfica
    fun observarEvidencia(actividadId: Long): Flow<EvidenciaEntity?>

    suspend fun obtenerEvidenciaPorActividadId(actividadId: Long): EvidenciaEntity?

    suspend fun guardarEvidenciaLocal(
        actividadId: Long,
        localUri: String,
        mimeType: String,
        tamano: Long
    ): EvidenciaEntity

    suspend fun subirEvidencia(
        context: Context,
        actividadId: Long
    ): RepositoryResult<Unit>

    suspend fun eliminarEvidencia(
        context: Context,
        actividadId: Long
    )
}
