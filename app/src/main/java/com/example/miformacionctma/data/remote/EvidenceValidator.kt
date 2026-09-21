package com.example.miformacionctma.data.remote

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Reglas de validación para las evidencias fotográficas.
 */
data class EvidencePolicy(
    val maxBytes: Long = 5L * 1024 * 1024 // Límite de 5 MB
)

/**
 * Metadatos extraídos y validados de una imagen.
 */
data class EvidenceMetadata(
    val mimeType: String,
    val sizeBytes: Long
)

/**
 * Valida tipo MIME, tamaño y legibilidad de una imagen a partir de su URI.
 */
fun validateImage(
    resolver: ContentResolver,
    uri: Uri,
    policy: EvidencePolicy = EvidencePolicy()
): Result<EvidenceMetadata> = runCatching {
    // 1. Validar Tipo MIME
    val type = resolver.getType(uri) ?: error("No se pudo determinar el tipo MIME de la imagen")
    require(type in setOf("image/jpeg", "image/png", "image/webp")) {
        "Formato no permitido ($type). Solo se admiten JPG, PNG y WEBP."
    }

    // 2. Validar Tamaño del archivo
    val size = querySize(resolver, uri)
    require(size in 1..policy.maxBytes) {
        if (size <= 0) "El archivo seleccionado está vacío"
        else "El archivo supera el tamaño máximo permitido de 5 MB"
    }

    // 3. Comprobar que el stream se pueda abrir y leer
    resolver.openInputStream(uri)?.use { stream ->
        require(stream.read() != -1) { "El archivo de imagen no contiene datos válidos" }
    } ?: error("No fue posible abrir el archivo de imagen")

    EvidenceMetadata(mimeType = type, sizeBytes = size)
}

/**
 * Consulta el tamaño real en bytes del archivo a través de ContentResolver.
 */
private fun querySize(resolver: ContentResolver, uri: Uri): Long {
    return resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex != -1 && cursor.moveToFirst()) {
            cursor.getLong(sizeIndex)
        } else 0L
    } ?: 0L
}