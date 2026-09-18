package com.example.miformacionctma.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EvidenciaApi {
    @POST("evidencias")
    suspend fun registrarEvidencia(
        @Body evidencia: EvidenciaDto
    ): Response<Unit>

    @DELETE("evidencias")
    suspend fun eliminarEvidencia(
        @Query("nombre_archivo") filtroNombre: String
    ): Response<Unit>

    @GET("evidencias")
    suspend fun obtenerEvidencias(
        @Query("actividad_id") filtroActividad: String
    ): Response<List<EvidenciaDto>>
}
