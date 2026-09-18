package com.example.miformacionctma.model

enum class EstadoEvidencia {
    LOCAL,
    SUBIENDO,
    SINCRONIZADA,
    FALLIDA
}

data class Evidencia(
    val id: Long = 0,
    val actividadId: Long,
    val uriLocal: String,
    val nombreArchivo: String,
    val mimeType: String,
    val tamanio: Long,
    val fechaCreacion: Long,
    val estado: EstadoEvidencia,
    val remoteUrl: String? = null,
    val finalidad: String? = null
)
