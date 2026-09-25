package com.example.miformacionctma.data.local.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Entidad que representa una evidencia fotográfica asociada a una actividad.
 * Se guarda la referencia local (URI) y sus metadatos, evitando guardar el binario en la base de datos.
 */
@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actividadId")]
)
data class EvidenciaEntity(
    @PrimaryKey val id: String,
    val actividadId: String,
    val localUri: String,
    val mimeType: String,
    val sizeBytes: Long,
    val estado: String, // LOCAL, SUBIENDO, SINCRONIZADA, FALLIDA
    val creadaEnEpochMillis: Long
)