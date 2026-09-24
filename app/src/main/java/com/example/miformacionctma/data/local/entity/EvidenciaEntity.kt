package com.example.miformacionctma.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant

/**
 * Entidad que almacena la referencia local y los metadatos de una evidencia fotográfica asociada
 * a una actividad formativa.
 * NO almacena imágenes ni cadenas Base64 directamente en la base de datos.
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
    indices = [
        Index(value = ["actividadId"])
    ]
)
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actividadId: Long,
    val localUri: String,
    val mimeType: String,
    val tamano: Long,
    val fecha: Instant,
    val estado: EstadoSincronizacion = EstadoSincronizacion.LOCAL
)
