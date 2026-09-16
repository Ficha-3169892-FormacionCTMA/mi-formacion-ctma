package com.example.miformacionctma.data.local

import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

fun ActividadEntity.toDomain(): ActividadFormativa {
    return ActividadFormativa(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = runCatching { Prioridad.valueOf(prioridad) }
            .getOrDefault(Prioridad.MEDIA)
    )
}

fun ActividadFormativa.toEntity(): ActividadEntity {
    return ActividadEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = prioridad.name
    )
}
