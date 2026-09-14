package com.example.miformacionctma.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CrearActividadDto(
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String
)