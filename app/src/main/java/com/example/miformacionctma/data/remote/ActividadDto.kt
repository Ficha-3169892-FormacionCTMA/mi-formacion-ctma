package com.example.miformacionctma.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de transporte que representa una actividad recibida desde la API REST.
 */
@Serializable
data class ActividadDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val progreso: Int,
    val prioridad: String,
    @SerialName("competencia_id") val competenciaId: String,
    @SerialName("fecha_limite") val fechaLimite: String,
    val completada: Boolean,
    @SerialName("actualizado_en") val actualizadoEn: String,
    @SerialName("aprendiz_id") val aprendizId: String? = null
)