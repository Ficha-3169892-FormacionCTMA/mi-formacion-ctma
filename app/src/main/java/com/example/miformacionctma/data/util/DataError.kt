package com.example.miformacionctma.data.util

sealed interface DataError {
    sealed interface Network : DataError {
        object NoConnection : Network
        object Timeout : Network
        object Unauthorized : Network
        object NotFound : Network
        object Server : Network
        object InvalidPayload : Network
        object Unknown : Network
    }

    sealed interface Local : DataError {
        object DiskFull : Local
    }
}
