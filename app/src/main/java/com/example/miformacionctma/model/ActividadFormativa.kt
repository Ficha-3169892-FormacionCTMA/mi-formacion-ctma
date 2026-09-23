package com.example.miformacionctma.model

import java.time.Instant

enum class Prioridad { BAJA, MEDIA, ALTA }

data class ActividadFormativa(
    val id: Long,
    val titulo: String,
    val descripcion: String = "",
    val fecha: Instant,
    val progreso: Int,
    val prioridad: Prioridad,
    val competenciaId: Long? = null,
    val completada: Boolean = false
)
