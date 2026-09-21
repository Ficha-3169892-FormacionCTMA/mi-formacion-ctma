package com.example.miformacionctma.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val progreso: Int,
    val prioridad: String,
    val competenciaId: String,
    val fechaLimite: String,
    val completada: Boolean
)