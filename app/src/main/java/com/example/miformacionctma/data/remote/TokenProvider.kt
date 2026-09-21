package com.example.miformacionctma.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Proveedor funcional para obtener el token de acceso de la sesión actual.
 */
fun interface TokenProvider {
    fun currentAccessToken(): String?
}

/**
 * Interceptor de OkHttp que adjunta el encabezado Authorization cuando existe un token válido.
 */
class BearerTokenInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.currentAccessToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}