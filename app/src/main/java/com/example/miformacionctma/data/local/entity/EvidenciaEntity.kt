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
    val uri: String, // Ruta del archivo en el almacenamiento interno de la app
    val tipo: String,
    val tamano: Long,
    val estado: String, // "PENDIENTE", "SINCRONIZADA", "FALLIDA"
    val urlRemota: String? = null // URL pública en Supabase Storage (null si aún no se ha subido)
) {
    companion object {
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_SINCRONIZADA = "SINCRONIZADA"
        const val ESTADO_FALLIDA = "FALLIDA"
    }
}
