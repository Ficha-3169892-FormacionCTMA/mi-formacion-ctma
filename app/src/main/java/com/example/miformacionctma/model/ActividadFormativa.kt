package com.example.miformacionctma.model

enum class Prioridad { BAJA, MEDIA, ALTA }

data class ActividadFormativa(
    val id: Long,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,
    val progreso: Int,
    val prioridad: Prioridad,
    val competenciaId: Long? = null
)
