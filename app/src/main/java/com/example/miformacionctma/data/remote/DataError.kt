package com.example.miformacionctma.data.remote

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