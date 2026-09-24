package com.example.miformacionctma.data.remote.api

import com.example.miformacionctma.data.remote.dto.ActividadDto
import com.example.miformacionctma.data.remote.dto.CompetenciaDto
import com.example.miformacionctma.data.remote.dto.PerfilDto
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Definición del contrato de red para los recursos de Actividades, Competencias, Perfiles y Evidencias.
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

    // Crear Actividad
    @POST("actividades")
    suspend fun crearActividad(@Body actividad: ActividadDto): Response<Unit>

    // Actualizar
    @PATCH("actividades")
    suspend fun actualizarActividad(
        @Query("id") idFilter: String,
        @Body actividad: ActividadDto
    ): Response<Unit>

    // Eliminar
    @DELETE("actividades")
    suspend fun eliminarActividad(
        @Query("id") idFilter: String
    ): Response<Unit>

    // Consultar Perfil de Usuario por Email
    @GET("perfiles")
    suspend fun getPerfilPorEmail(
        @Query("email") emailFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<PerfilDto>>

    // Crear Perfil de Usuario
    @POST("perfiles")
    suspend fun crearPerfil(@Body perfil: PerfilDto): Response<Unit>

    // Subir Evidencia Fotográfica directamente al endpoint de Supabase Storage
    @POST
    suspend fun subirEvidencia(
        @Url url: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Header("x-upsert") xUpsert: String = "true",
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): Response<Unit>
}
