package com.example.miformacionctma.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Contrato de endpoints HTTP con Retrofit para el recurso Actividades.
 */
interface ActividadesApi {

    @GET("v1/actividades")
    suspend fun listar(): Response<List<ActividadDto>>

    @GET("v1/actividades/{id}")
    suspend fun obtener(@Path("id") id: String): Response<ActividadDto>

    @POST("v1/actividades")
    suspend fun crear(@Body solicitud: ActividadDto): Response<ActividadDto>
}