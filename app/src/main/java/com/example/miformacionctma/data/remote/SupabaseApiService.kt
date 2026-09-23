package com.example.miformacionctma.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

// Fila que se inserta en la tabla 'evidencias' de Supabase
@Serializable
data class EvidenciaDTO(
    val actividad_id: String,
    val url_remota: String,
    val tipo_mime: String,
    val tamano_bytes: Long
)

// Fila leída desde Supabase. actividad_id se lee como JsonPrimitive para aceptar columnas text o int
@Serializable
data class EvidenciaRemotaDTO(
    @SerialName("actividad_id") val actividadId: JsonPrimitive,
    @SerialName("url_remota") val urlRemota: String,
    @SerialName("tipo_mime") val tipoMime: String = "image/jpeg",
    @SerialName("tamano_bytes") val tamanoBytes: Long = 0
)

// Fila de la tabla 'actividades' en Supabase (ver docs/supabase/tabla_actividades.sql)
@Serializable
data class ActividadRemotaDTO(
    val id: Long,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,
    val prioridad: String = "MEDIA",
    val progreso: Int = 0,
    val completada: Boolean = false
)

interface SupabaseApiService {

    @GET("rest/v1/actividades")
    suspend fun listActividades(
        @Query("select") select: String = "id,titulo,descripcion,fecha,prioridad,progreso,completada"
    ): Response<List<ActividadRemotaDTO>>

    // Inserta o actualiza según el id (upsert de PostgREST)
    @Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("rest/v1/actividades")
    suspend fun upsertActividad(
        @Body actividad: ActividadRemotaDTO
    ): Response<Unit>

    // id usa el formato de PostgREST: "eq.123"
    @DELETE("rest/v1/actividades")
    suspend fun deleteActividad(
        @Query("id") id: String
    ): Response<Unit>

    // Subir archivo binario a Supabase Storage (x-upsert sobrescribe si ya existe)
    @Headers("x-upsert: true")
    @POST("storage/v1/object/${SupabaseConfig.BUCKET}/{fileName}")
    suspend fun uploadEvidenciaFile(
        @Path("fileName") fileName: String,
        @Body fileBytes: RequestBody
    ): Response<Unit>

    // Borrar archivo de Supabase Storage
    @DELETE("storage/v1/object/${SupabaseConfig.BUCKET}/{fileName}")
    suspend fun deleteEvidenciaFile(
        @Path("fileName") fileName: String
    ): Response<Unit>

    // Descargar la imagen desde su URL pública
    @Streaming
    @GET
    suspend fun downloadEvidenciaFile(@Url url: String): Response<ResponseBody>

    // Insertar registro en la tabla SQL 'evidencias'
    @POST("rest/v1/evidencias")
    suspend fun insertEvidenciaData(
        @Body evidencia: EvidenciaDTO
    ): Response<Unit>

    // Listar registros de la tabla 'evidencias'. actividadId usa el formato de PostgREST: "eq.123"
    @GET("rest/v1/evidencias")
    suspend fun listEvidenciaData(
        @Query("select") select: String = "actividad_id,url_remota,tipo_mime,tamano_bytes",
        @Query("actividad_id") actividadId: String? = null
    ): Response<List<EvidenciaRemotaDTO>>

    // Borrar los registros de una actividad. actividadId usa el formato de PostgREST: "eq.123"
    @DELETE("rest/v1/evidencias")
    suspend fun deleteEvidenciaData(
        @Query("actividad_id") actividadId: String
    ): Response<Unit>
}

object SupabaseConfig {
    const val BUCKET = "evidencias_bucket"
}
