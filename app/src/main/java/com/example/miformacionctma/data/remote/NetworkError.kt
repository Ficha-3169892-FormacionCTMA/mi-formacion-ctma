package com.example.miformacionctma.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException

sealed interface DataError {
    data object NoConnection : DataError
    data object Timeout : DataError
    data object Unauthorized : DataError
    data object NotFound : DataError
    data class Server(val code: Int) : DataError
    data object InvalidPayload : DataError
    data class Unknown(val cause: Throwable) : DataError
}

class NetworkFailure(val error: DataError) : Exception()

/**
 * Ejecuta un bloque de red y clasifica las excepciones conocidas.
 * Retorna siempre un Result.success(...) o un Result.failure(...).
 */
suspend fun <T> classifyNetworkCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled // Regla estricta: re-lanzar para permitir la cancelación adecuada
} catch (failure: NetworkFailure) {
    Result.failure(failure) // <-- AQUÍ: Retornamos Result.failure con la falla existente
} catch (timeout: SocketTimeoutException) {
    Result.failure(NetworkFailure(DataError.Timeout))
} catch (io: IOException) {
    Result.failure(NetworkFailure(DataError.NoConnection))
} catch (serialization: SerializationException) {
    Result.failure(NetworkFailure(DataError.InvalidPayload))
} catch (e: Exception) {
    Result.failure(NetworkFailure(DataError.Unknown(e)))
}