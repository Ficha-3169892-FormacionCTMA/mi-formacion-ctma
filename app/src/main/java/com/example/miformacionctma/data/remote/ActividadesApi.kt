package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.remote.model.ActividadDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ActividadesApi {
    @GET("v1/actividades")
    suspend fun listar(): Response<List<ActividadDto>>

    @GET("v1/actividades/{id}")
    suspend fun obtener(@Path("id") id: String): Response<ActividadDto>

    @Multipart
    @POST("v1/actividades/{id}/evidencia")
    suspend fun subirEvidencia(
        @Path("id") id: String,
        @Part parte: MultipartBody.Part
    ): Response<Unit>
}