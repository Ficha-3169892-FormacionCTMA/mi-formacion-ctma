package com.example.miformacionctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvidenciaDto(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("actividad_id")
    val actividadId: Long,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("tamano")
    val tamano: Long,
    @SerialName("estado")
    val estado: String = "SINCRONIZADA"
)
