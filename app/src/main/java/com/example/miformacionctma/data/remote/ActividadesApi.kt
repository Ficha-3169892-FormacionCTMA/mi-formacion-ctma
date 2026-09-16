package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.remote.model.ActividadDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ActividadesApi {
    @GET("v1/actividades")
    suspend fun listar(): Response<List<ActividadDto>>

    @GET("v1/actividades/{id}")
    suspend fun obtener(@Path("id") id: String): Response<ActividadDto>
}