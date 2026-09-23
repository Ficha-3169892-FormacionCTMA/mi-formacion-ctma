package com.example.miformacionctma.data.remote.api

import com.example.miformacionctma.data.remote.dto.ActividadDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Definición del contrato de red para el recurso de Actividades.
 */
interface ActividadesApi {
    // Solo apuntamos al recurso 'actividades'. El path base se define en la configuración.
    @GET("actividades")
    suspend fun getActividades(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Response<List<ActividadDto>>
}
