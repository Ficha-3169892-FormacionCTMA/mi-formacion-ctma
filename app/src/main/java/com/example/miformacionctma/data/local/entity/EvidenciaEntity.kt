package com.example.miformacionctma.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EvidenciaEntity(
    @PrimaryKey
    val actividadId: Int,
    val uri: String,
    val tipo: String,
    val tamano: Long,
    val estado: String // "LOCAL", "SUBIENDO", "SINCRONIZADA", "FALLIDA"
)
