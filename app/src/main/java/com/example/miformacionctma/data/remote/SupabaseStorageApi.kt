package com.example.miformacionctma.data.remote

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {

    @POST("object/{bucket}/{path}")
    suspend fun subirArchivo(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body archivo: RequestBody,
        @Header("x-upsert") upsert: String = "true"
    ): Response<Unit>

    @DELETE("object/{bucket}/{path}")
    suspend fun eliminarArchivo(
        @Path("bucket") bucket: String,
        @Path("path") path: String
    ): Response<Unit>
}
