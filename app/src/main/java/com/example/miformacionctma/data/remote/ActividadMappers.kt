package com.example.miformacionctma.data.remote

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

fun ActividadDto.toDomain(): ActividadFormativa {
    return ActividadFormativa(
        id = id,
        titulo = titulo,
        descripcion = descripcion ?: "",
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = Prioridad.valueOf(prioridad.uppercase())
    )
}