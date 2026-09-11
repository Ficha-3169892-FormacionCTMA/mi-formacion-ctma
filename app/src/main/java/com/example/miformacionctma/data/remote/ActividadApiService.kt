package com.example.miformacionctma.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ActividadApiService {

    @GET("v1/actividades")
    suspend fun getActividades(): Response<List<ActividadDto>>

    @GET("v1/actividades/{id}")
    suspend fun getActividad(@Path("id") id: Long): Response<ActividadDto>

    @POST("v1/actividades")
    suspend fun createActividad(@Body actividad: ActividadDto): Response<ActividadDto>

    @PUT("v1/actividades/{id}")
    suspend fun updateActividad(
        @Path("id") id: Long,
        @Body actividad: ActividadDto
    ): Response<ActividadDto>
}
