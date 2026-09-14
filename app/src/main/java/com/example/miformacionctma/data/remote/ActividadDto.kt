package com.example.miformacionctma.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ActividadDto(
    val id: Long,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String
)