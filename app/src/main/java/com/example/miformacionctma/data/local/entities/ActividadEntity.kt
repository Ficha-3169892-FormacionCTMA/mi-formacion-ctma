package com.example.miformacionctma.data.local.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "actividades",
    foreignKeys = [ForeignKey(
        entity = CompetenciaEntity::class,
        parentColumns = ["id"],
        childColumns = ["competenciaId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("competenciaId"), Index(value = ["titulo"])]
)
data class ActividadEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String?,
    val progreso: Int,
    val prioridad: String,
    val competenciaId: Long,
    val fechaLimiteEpochMillis: Long,
    val completada: Boolean = false
)
