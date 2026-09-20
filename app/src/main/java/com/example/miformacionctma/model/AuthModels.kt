package com.example.miformacionctma.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement

@Serializable
enum class Rol {
    @SerialName("INSTRUCTOR")
    INSTRUCTOR,
    @SerialName("ESTUDIANTE")
    ESTUDIANTE
}

@Serializable
data class Usuario(
    val id: String,
    val email: String,
    @SerialName("nombre_completo") val nombreCompleto: String?,
    val rol: Rol
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String?,
    val user: SupabaseUser
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String,
    @SerialName("user_metadata") val userMetadata: Map<String, JsonElement>? = null
)
