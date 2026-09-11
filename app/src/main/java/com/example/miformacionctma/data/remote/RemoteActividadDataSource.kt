package com.example.miformacionctma.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RemoteActividadDataSource(
    private val apiService: ActividadApiService
) {

    suspend fun getActividades(): List<ActividadDto> {
        return safeApiCall {
            apiService.getActividades()
        } ?: emptyList()
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): T? {
        try {
            val response = call()
            if (response.isSuccessful) {
                return response.body()
            } else {
                when (response.code()) {
                    401 -> throw NetworkException.NoAutorizado()
                    404 -> throw NetworkException.NoEncontrado()
                    in 500..599 -> throw NetworkException.ErrorServidor(response.code())
                    else -> throw NetworkException.Desconocido("Error HTTP: ${response.code()}")
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: NetworkException) {
            throw e
        } catch (e: UnknownHostException) {
            throw NetworkException.SinConexion(e)
        } catch (e: SocketTimeoutException) {
            throw NetworkException.Timeout(e)
        } catch (e: SerializationException) {
            throw NetworkException.FormatoInvalido(e)
        } catch (e: IOException) {
            throw NetworkException.Desconocido(e.message ?: "Error de E/S")
        } catch (e: Exception) {
            throw NetworkException.Desconocido(e.message ?: "Error inesperado")
        }
    }
}
