package com.example.miformacionctma.model

enum class Prioridad { BAJA, MEDIA, ALTA }

data class ActividadFormativa(
    val id: String,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: Prioridad,
    val competenciaId: Long = 0L,
    val fechaLimiteEpochMillis: Long = 0L,
    val completada: Boolean = false,
    val aprendizId: String? = null
)