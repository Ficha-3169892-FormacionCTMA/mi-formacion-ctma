package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.local.ActividadEntity
import kotlinx.serialization.Serializable

@Serializable
data class ActividadDto(
    val id: Long,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String
)

fun ActividadDto.toEntity(): ActividadEntity {
    return ActividadEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = prioridad
    )
}

fun ActividadEntity.toDto(): ActividadDto {
    return ActividadDto(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = prioridad
    )
}
