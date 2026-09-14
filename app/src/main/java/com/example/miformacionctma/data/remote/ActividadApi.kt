package com.example.miformacionctma.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Header

interface ActividadApi {

    @GET("actividades")
    suspend fun obtenerActividades(): Response<List<ActividadDto>>

    @GET("actividades")
    suspend fun obtenerActividad(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): Response<List<ActividadDto>>

    @POST("actividades")
    suspend fun crearActividad(
        @Header("Prefer") prefer: String = "return=representation",
        @Body actividad: CrearActividadDto
    ): Response<List<ActividadDto>>

    @PATCH("actividades")
    suspend fun actualizarActividad(
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") id: String,
        @Query("select") select: String = "*",
        @Body actividad: CrearActividadDto
    ): Response<List<ActividadDto>>

    @DELETE("actividades")
    suspend fun eliminarActividad(
        @Query("id") id: String
    ): Response<Unit>
}