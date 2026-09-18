package com.example.miformacionctma.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EvidenciaDto(
    @SerialName("actividad_id") val actividadId: Long,
    @SerialName("nombre_archivo") val nombreArchivo: String,
    @SerialName("remote_url") val remoteUrl: String,
    @SerialName("mime_type") val mimeType: String
)
