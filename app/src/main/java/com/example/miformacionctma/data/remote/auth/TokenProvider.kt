package com.example.miformacionctma.data.remote.auth

/**
 * Interfaz para proveer el token de autenticación de forma dinámica.
 * Evita el hardcoding de credenciales.
 */
interface TokenProvider {
    fun getToken(): String?
}

/**
 * Implementación de ejemplo (debe conectarse con un sistema de almacenamiento seguro como DataStore o EncryptedSharedPreferences).
 */
class SessionTokenProvider : TokenProvider {
    override fun getToken(): String? {
        // En una implementación real, esto se obtendría de una fuente segura.
        // NUNCA guardar tokens reales aquí.
        return null 
    }
}
