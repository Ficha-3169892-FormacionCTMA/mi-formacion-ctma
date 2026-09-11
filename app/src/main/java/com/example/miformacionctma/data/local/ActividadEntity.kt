package com.example.miformacionctma.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = CompetenciaEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenciaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("competenciaId"),
        Index("titulo")
    ]
)
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val prioridad: String,
    val progreso: Int,
    val competenciaId: Long = 1,
    val completada: Boolean = false
)