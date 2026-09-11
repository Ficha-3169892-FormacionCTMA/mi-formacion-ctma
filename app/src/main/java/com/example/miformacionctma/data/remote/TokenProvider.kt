package com.example.miformacionctma.data.remote

interface TokenProvider {
    suspend fun getToken(): String?
}

class StaticTokenProvider : TokenProvider {
    override suspend fun getToken(): String? {
        // En una app real, esto vendría de DataStore o EncryptedSharedPreferences
        // Para este lab, devolvemos un valor conceptual
        return "TOKEN_CONCEPTUAL_DE_PRUEBA"
    }
}
