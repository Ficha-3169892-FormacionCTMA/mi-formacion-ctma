package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.local.ActividadEntity

// Asegúrate de importar tu ActividadEntity si está en otro paquete
// import com.example.miformacionctma.data.local.ActividadEntity


fun ActividadDto.toEntity(): ActividadEntity {
    require(progreso in 0..100) { "El progreso remoto debe estar entre 0 y 100" }

    return ActividadEntity(
        id = id,
        titulo = titulo.trim(),
        descripcion = descripcion.trim(),
        progreso = progreso,
        prioridad = prioridad.uppercase(),
        competenciaId = competenciaId,
        fechaLimite = fechaLimite,
        completada = completada
    )
}