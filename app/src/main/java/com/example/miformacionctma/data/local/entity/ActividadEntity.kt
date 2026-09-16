package com.example.miformacionctma.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String
)
