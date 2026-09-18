package com.example.miformacionctma.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

class RemoteEvidenciaDataSource(
    private val storageApi: SupabaseStorageApi,
    private val evidenciaApi: EvidenciaApi
) {

    suspend fun subirEvidencia(
        bucket: String,
        path: String,
        bytes: ByteArray,
        mimeType: String,
        actividadId: Long,
        nombreArchivo: String,
        remoteUrl: String
    ) {
        // 1. Subir archivo
        val uploadResponse = storageApi.subirArchivo(
            bucket = bucket,
            path = path,
            archivo = bytes.toRequestBody("application/octet-stream".toMediaType())
        )

        if (!uploadResponse.isSuccessful) {
            manejarErrorHttp(uploadResponse)
        }

        // 2. Registrar en tabla de base de datos
        // NOTA: Si la tabla no existe en Supabase, este paso fallará (404).
        // Si prefieres que solo suba el archivo sin registro en DB, comenta este bloque.
        try {
            val dbResponse = evidenciaApi.registrarEvidencia(
                EvidenciaDto(
                    actividadId = actividadId,
                    nombreArchivo = nombreArchivo,
                    remoteUrl = remoteUrl,
                    mimeType = mimeType
                )
            )
            if (!dbResponse.isSuccessful) {
                // Opcional: loguear error de DB pero no fallar la sincronización completa 
                // si el archivo ya está en el storage. 
                // Pero por ahora lo manejamos como fallo.
                manejarErrorHttp(dbResponse)
            }
        } catch (e: Exception) {
            // Si falla el registro en la tabla, el archivo ya está arriba.
            // Podríamos ignorar este error si no tienes la tabla creada.
            if (e.message?.contains("404") == true) {
                // Tabla no existe, ignoramos.
            } else {
                throw e
            }
        }
    }

    suspend fun obtenerEvidencias(actividadId: Long): List<EvidenciaDto> {
        return ejecutarPeticion {
            evidenciaApi.obtenerEvidencias("eq.$actividadId")
        } ?: emptyList()
    }

    suspend fun eliminarEvidencia(
        bucket: String,
        path: String,
        nombreArchivo: String
    ) {
        // 1. Eliminar del Storage
        ejecutarPeticion {
            storageApi.eliminarArchivo(bucket, path)
        }

        // 2. Eliminar de la Base de Datos
        try {
            ejecutarPeticion {
                evidenciaApi.eliminarEvidencia("eq.$nombreArchivo")
            }
        } catch (e: Exception) {
            // Si la tabla no existe o ya se borró, ignoramos para no bloquear el flujo
        }
    }

    private suspend fun <T> ejecutarPeticion(
        peticion: suspend () -> Response<T>
    ): T? {
        try {
            val response = peticion()

            if (response.isSuccessful) {
                return response.body()
            }

            manejarErrorHttp(response)

        } catch (error: CancellationException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw Exception("La conexión tardó demasiado tiempo.", error)
        } catch (error: IOException) {
            throw Exception("No hay conexión con el servidor.", error)
        } catch (error: HttpException) {
            throw Exception("Error de comunicación con el servidor.", error)
        }
        return null
    }

    private fun manejarErrorHttp(response: Response<*>): Nothing {
        val errorBody = response.errorBody()?.string()
        val mensajeBase = when (response.code()) {
            400, 422 -> "Datos no válidos"
            401 -> "No autorizado"
            404 -> "Recurso no encontrado"
            in 500..599 -> "Error del servidor"
            else -> "Error HTTP ${response.code()}"
        }
        
        throw Exception("$mensajeBase: $errorBody")
    }
}
