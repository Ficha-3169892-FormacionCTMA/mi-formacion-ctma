package com.example.miformacionctma.data.repository

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import kotlinx.coroutines.flow.Flow

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
}
