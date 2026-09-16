package com.example.miformacionctma.data.remote.model

import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.model.Prioridad
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActividadDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val progreso: Int,
    val prioridad: String,
    @SerialName("competencia_id") val competenciaId: String = "",
    @SerialName("fecha_limite") val fechaLimite: String,
    val completada: Boolean = false,
    @SerialName("actualizado_en") val actualizadoEn: String = ""
)

// Data Mapper: Transforma el DTO remoto a la entidad local de Room
fun ActividadDto.toEntity(): ActividadEntity {
    require(progreso in 0..100) { "Progreso remoto fuera de rango: $progreso" }

    // Convierte el id recibido en String a Int (extrayendo solo dígitos o usando hash si no es numérico)
    val idNumerico = id.filter { it.isDigit() }.toIntOrNull() ?: id.hashCode()

    // Mapea el texto de prioridad al Enum de la app
    val prioridadEnum = try {
        Prioridad.valueOf(prioridad.uppercase())
    } catch (e: Exception) {
        Prioridad.MEDIA
    }

    return ActividadEntity(
        id = idNumerico,
        titulo = titulo.trim(),
        descripcion = descripcion.trim(),
        progreso = progreso,
        prioridad = prioridadEnum,
        fecha = fechaLimite
    )
}