package com.example.miformacionctma.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.miformacionctma.model.Evidencia
import com.example.miformacionctma.model.EstadoEvidencia

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
    indices = [Index(value = ["actividadId"])]
)
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actividadId: Long,
    val uriLocal: String,
    val nombreArchivo: String,
    val mimeType: String,
    val tamanio: Long,
    val fechaCreacion: Long,
    val estado: String, // Se manejará via Converters para mapear a EstadoEvidencia
    val remoteUrl: String? = null,
    val finalidad: String? = null
)

fun EvidenciaEntity.toDomain(): Evidencia {
    return Evidencia(
        id = id,
        actividadId = actividadId,
        uriLocal = uriLocal,
        nombreArchivo = nombreArchivo,
        mimeType = mimeType,
        tamanio = tamanio,
        fechaCreacion = fechaCreacion,
        estado = EstadoEvidencia.valueOf(estado),
        remoteUrl = remoteUrl,
        finalidad = finalidad
    )
}

fun Evidencia.toEntity(): EvidenciaEntity {
    return EvidenciaEntity(
        id = id,
        actividadId = actividadId,
        uriLocal = uriLocal,
        nombreArchivo = nombreArchivo,
        mimeType = mimeType,
        tamanio = tamanio,
        fechaCreacion = fechaCreacion,
        estado = estado.name,
        remoteUrl = remoteUrl,
        finalidad = finalidad
    )
}
