package com.example.miformacionctma.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ActividadDto(
    val id: Long,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String,
    @SerialName("estudiante_id") val estudianteId: String? = null,
    @SerialName("perfil_estudiante") val perfilEstudiante: PerfilCortoDto? = null
)

@Serializable
data class PerfilCortoDto(
    @SerialName("nombre_completo") val nombreCompleto: String?
)
