package com.example.miformacionctma.data.local

import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

fun ActividadEntity.toDomain() = ActividadFormativa(
    id = id,
    titulo = titulo,
    descripcion = descripcion ?: "",
    fecha = "", // we can leave this or map from epoch millis if needed, but let's keep it empty or default
    progreso = progreso,
    diasRestantes = 7, // default or calculated
    prioridad = Prioridad.valueOf(prioridad),
    competenciaId = competenciaId,
    fechaLimiteEpochMillis = fechaLimiteEpochMillis,
    completada = completada
)

fun ActividadFormativa.toEntity() = ActividadEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    progreso = progreso,
    prioridad = prioridad.name,
    competenciaId = competenciaId,
    fechaLimiteEpochMillis = fechaLimiteEpochMillis,
    completada = completada
)
