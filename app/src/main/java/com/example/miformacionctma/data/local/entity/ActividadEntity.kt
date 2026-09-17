package com.example.miformacionctma.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "actividades",
    indices = [
        Index(value = ["competenciaId"])
    ]
)

data class ActividadEntity(
    @PrimaryKey
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val progreso: Int,
    val prioridad: String,
    val competenciaId: Long? = null,
    val completada: Boolean = false
)
