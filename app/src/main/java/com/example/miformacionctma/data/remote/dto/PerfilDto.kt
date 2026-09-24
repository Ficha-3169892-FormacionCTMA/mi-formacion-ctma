package com.example.miformacionctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerfilDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("email")
    val email: String,
    @SerialName("nombre")
    val nombre: String,
    @SerialName("rol")
    val rol: String,
    @SerialName("password")
    val password: String? = null
)
