package com.example.miformacionctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActividadDto(
    @SerialName("id")
    val id: Long,
    @SerialName("titulo")
    val titulo: String,
    @SerialName("descripcion")
    val descripcion: String,
    @SerialName("fecha")
    val fecha: String,
    @SerialName("progreso")
    val progreso: Int,
    @SerialName("prioridad")
    val prioridad: String,
    @SerialName("competencia_id")
    val competenciaId: Long? = null,
    @SerialName("esta_completada")
    val estaCompletada: Boolean = false
)
