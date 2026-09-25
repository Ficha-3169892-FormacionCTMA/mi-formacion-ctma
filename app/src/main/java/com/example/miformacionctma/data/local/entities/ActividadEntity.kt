package com.example.miformacionctma.data.local.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val descripcion: String,
    val progreso: Int,
    val prioridad: String,
    val competenciaId: Long = 1L,
    val fechaLimiteEpochMillis: Long = 0L,
    val completada: Boolean = false,
    val fecha: String = "",
    val diasRestantes: Int = 0,
    val aprendizId: String? = null // 👈 Agregado para almacenar el ID del aprendiz asignado
)