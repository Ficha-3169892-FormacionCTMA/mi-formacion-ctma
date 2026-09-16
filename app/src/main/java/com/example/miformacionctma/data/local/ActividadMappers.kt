package com.example.miformacionctma.data.local

import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.CompetenciaEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import com.example.miformacionctma.model.Prioridad

fun ActividadEntity.toDomain(): ActividadFormativa {
    return ActividadFormativa(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        prioridad = runCatching { Prioridad.valueOf(prioridad) }
            .getOrDefault(Prioridad.MEDIA),
        competenciaId = competenciaId
    )
}

fun ActividadFormativa.toEntity(): ActividadEntity {
    return ActividadEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        prioridad = prioridad.name,
        competenciaId = competenciaId
    )
}

fun CompetenciaEntity.toDomain(): Competencia {
    return Competencia(
        id = id,
        nombre = nombre
    )
}
