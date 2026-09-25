package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

fun ActividadDto.toEntity(): ActividadEntity {
    require(progreso in 0..100) { "El progreso remoto debe estar entre 0 y 100" }

    return ActividadEntity(
        id = id,
        titulo = titulo.trim(),
        descripcion = descripcion.trim(),
        progreso = progreso,
        prioridad = prioridad.uppercase(),
        competenciaId = competenciaId.toLongOrNull() ?: 1L,
        fecha = fechaLimite,
        completada = completada,
        aprendizId = aprendizId
    )
}

fun ActividadEntity.toDomain() = ActividadFormativa(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fecha = fecha,
    progreso = progreso,
    diasRestantes = diasRestantes,
    prioridad = try { Prioridad.valueOf(prioridad) } catch (_: Exception) { Prioridad.MEDIA },
    competenciaId = competenciaId,
    fechaLimiteEpochMillis = fechaLimiteEpochMillis,
    completada = completada,
    aprendizId = aprendizId
)

fun ActividadFormativa.toEntity() = ActividadEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    progreso = progreso,
    prioridad = prioridad.name,
    competenciaId = competenciaId,
    fechaLimiteEpochMillis = fechaLimiteEpochMillis,
    completada = completada,
    fecha = fecha,
    diasRestantes = diasRestantes,
    aprendizId = aprendizId
)