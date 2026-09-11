package com.example.miformacionctma.data.remote

import java.io.IOException

sealed class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class SinConexion(cause: Throwable) : NetworkException("Sin conexión a Internet", cause)
    class Timeout(cause: Throwable) : NetworkException("Tiempo de espera agotado", cause)
    class NoAutorizado : NetworkException("Sesión expirada o no autorizada")
    class NoEncontrado : NetworkException("Recurso no encontrado")
    class ErrorServidor(codigo: Int) : NetworkException("Error del servidor ($codigo)")
    class FormatoInvalido(cause: Throwable) : NetworkException("Error al procesar los datos", cause)
    class Desconocido(message: String) : NetworkException(message)
}
