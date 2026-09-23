package com.example.miformacionctma.data.remote.api

import com.example.miformacionctma.data.remote.dto.ActividadDto
import com.example.miformacionctma.data.remote.dto.CompetenciaDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Definición del contrato de red para el recurso de Actividades.
 */
interface ActividadesApi {
    // Listar Actividades
    @GET("actividades")
    suspend fun getActividades(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Response<List<ActividadDto>>

    // Listar Competencias
    @GET("competencias")
    suspend fun getCompetencias(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Response<List<CompetenciaDto>>

    // Crear
    @POST("actividades")
    suspend fun crearActividad(@Body actividad: ActividadDto): Response<Unit>

    // Actualizar (usamos PATCH para actualizaciones parciales/seguras)
    @PATCH("actividades")
    suspend fun actualizarActividad(
        @Query("id") idFilter: String, // ej: "eq.101"
        @Body actividad: ActividadDto
    ): Response<Unit>

    // Eliminar
    @DELETE("actividades")
    suspend fun eliminarActividad(
        @Query("id") idFilter: String // ej: "eq.101"
    ): Response<Unit>
}
