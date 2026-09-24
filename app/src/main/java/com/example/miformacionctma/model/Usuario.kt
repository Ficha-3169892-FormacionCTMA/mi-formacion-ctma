package com.example.miformacionctma.model

/**
 * Modelo de datos del usuario autenticado en la sesión actual.
 */
data class Usuario(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: RolUsuario
)
