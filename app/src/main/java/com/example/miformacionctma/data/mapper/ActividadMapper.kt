package com.example.miformacionctma.data.mapper

import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.remote.dto.ActividadDto
import com.example.miformacionctma.model.Prioridad
import java.time.Instant

/**
 * Mapea un DTO de red a una Entidad de Room.
 * Realiza validaciones de negocio y transformaciones de tipos.
 */
fun ActividadDto.toEntity(): ActividadEntity {
    // Validación de negocio: Progreso entre 0 y 100
    val progresoValidado = progreso.coerceIn(0, 100)

    // Validación de negocio: Mapeo seguro de Prioridad
    val prioridadNormalizada = try {
        Prioridad.valueOf(prioridad.uppercase()).name
    } catch (e: Exception) {
        Prioridad.MEDIA.name
    }

    // Transformación robusta de fecha (ISO 8601 a Instant)
    val fechaRobust = try {
        Instant.parse(fecha)
    } catch (e: Exception) {
        Instant.now() // fallback seguro
    }

    return ActividadEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fechaRobust,
        progreso = progresoValidado,
        prioridad = prioridadNormalizada,
        competenciaId = competenciaId,
        completada = estaCompletada
    )
}

/**
 * Mapea una lista de DTOs a una lista de Entidades.
 */
fun List<ActividadDto>.toEntityList(): List<ActividadEntity> {
    return map { it.toEntity() }
}
