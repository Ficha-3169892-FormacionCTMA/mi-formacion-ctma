package com.example.miformacionctma.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response

class RemoteActividadDataSource(
    private val api: ActividadApi
) {

    suspend fun obtenerActividades(): List<ActividadDto> {
        return ejecutarPeticion {
            api.obtenerActividades()
        }
    }

    suspend fun obtenerActividad(id: Long): ActividadDto {
        val lista = ejecutarPeticion {
            api.obtenerActividad("eq.$id")
        }
        if (lista.isEmpty()) {
            throw Exception("No se encontró la actividad.")
        }
        return lista.first()
    }

    suspend fun crearActividad(
        actividad: CrearActividadDto
    ): ActividadDto {
        val lista = ejecutarPeticion {
            api.crearActividad(actividad = actividad)
        }
        if (lista.isEmpty()) {
            throw Exception("El servidor no devolvió la actividad creada.")
        }
        return lista.first()
    }

    suspend fun actualizarActividad(
        id: Long,
        actividad: CrearActividadDto
    ): ActividadDto {
        val lista = ejecutarPeticion {
            api.actualizarActividad(
                id = "eq.$id",
                actividad = actividad
            )
        }
        if (lista.isEmpty()) {
            throw Exception("El servidor no encontró el registro para actualizar.")
        }
        return lista.first()
    }

    suspend fun eliminarActividad(id: Long) {
        try {
            val response = api.eliminarActividad("eq.$id")

            if (response.isSuccessful) {
                return
            }

            manejarErrorHttp(response)

        } catch (error: CancellationException) {
            throw error

        } catch (error: SerializationException) {
            throw Exception(
                "Error de serialización en la respuesta del servidor.",
                error
            )

        } catch (error: SocketTimeoutException) {
            throw Exception(
                "La conexión tardó demasiado tiempo.",
                error
            )

        } catch (error: IOException) {
            throw Exception(
                "No hay conexión con el servidor.",
                error
            )

        } catch (error: HttpException) {
            throw Exception(
                "Error de comunicación con el servidor.",
                error
            )
        }
    }

    private suspend fun <T> ejecutarPeticion(
        peticion: suspend () -> Response<T>
    ): T {

        try {
            val response = peticion()

            if (response.isSuccessful) {
                return response.body()
                    ?: throw Exception(
                        "El servidor devolvió una respuesta vacía."
                    )
            }

            manejarErrorHttp(response)

        } catch (error: CancellationException) {
            throw error

        } catch (error: SerializationException) {
            throw Exception(
                "Error de serialización en la respuesta del servidor.",
                error
            )

        } catch (error: SocketTimeoutException) {
            throw Exception(
                "La conexión tardó demasiado tiempo.",
                error
            )

        } catch (error: IOException) {
            throw Exception(
                "No hay conexión con el servidor.",
                error
            )

        } catch (error: HttpException) {
            throw Exception(
                "Error de comunicación con el servidor.",
                error
            )
        }
    }

    private fun manejarErrorHttp(
        response: Response<*>
    ): Nothing {

        when (response.code()) {

            400, 422 -> {
                throw Exception(
                    "Los datos enviados no son válidos."
                )
            }

            401 -> {
                throw Exception(
                    "No autorizado. Verifica las credenciales del servidor."
                )
            }

            404 -> {
                throw Exception(
                    "El recurso solicitado no existe."
                )
            }

            409 -> {
                throw Exception(
                    "El recurso no pudo actualizarse por un conflicto."
                )
            }

            in 500..599 -> {
                throw Exception(
                    "El servidor no está disponible en este momento."
                )
            }

            else -> {
                throw Exception(
                    "Error HTTP ${response.code()}: ${response.message()}"
                )
            }
        }
    }
}