package com.example.miformacionctma.data.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Resultado de la validación de un archivo de evidencia fotográfica.
 */
sealed class ResultadoValidacionEvidencia {
    data class Exito(
        val uriLocalFinal: Uri,
        val mimeType: String,
        val tamanoBytes: Long
    ) : ResultadoValidacionEvidencia()

    data class Error(
        val mensaje: String
    ) : ResultadoValidacionEvidencia()
}

/**
 * Validador de evidencias mediante ContentResolver.
 * Comprueba el tipo MIME, el tamaño máximo y la capacidad de leer el stream.
 */
object EvidenciaValidador {

    private val MIME_TYPES_PERMITIDOS = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    )

    // Límite de 5 MB por evidencia
    const val LIMITE_TAMANO_BYTES = 5 * 1024 * 1024L

    /**
     * Valida un archivo de imagen utilizando ContentResolver.
     * Si es válido, copia el archivo de forma segura al directorio de evidencias interno
     * y devuelve un URI local tipo file:// totalmente accesible por Coil y Room.
     */
    fun validarYProcesarEvidencia(
        context: Context,
        uriOrigen: Uri,
        limiteBytes: Long = LIMITE_TAMANO_BYTES
    ): ResultadoValidacionEvidencia {
        val contentResolver = context.contentResolver

        // 1. Comprobar que se puede abrir el stream de lectura
        val inputStream: InputStream = try {
            contentResolver.openInputStream(uriOrigen)
                ?: return ResultadoValidacionEvidencia.Error("No se pudo abrir el stream de lectura para la imagen seleccionada.")
        } catch (e: Exception) {
            return ResultadoValidacionEvidencia.Error("Error al acceder al archivo: ${e.localizedMessage}")
        }

        // 2. Extraer y verificar tipo MIME
        val mimeTypeRaw = contentResolver.getType(uriOrigen)
        val mimeType = mimeTypeRaw?.lowercase() ?: "image/jpeg"

        if (!MIME_TYPES_PERMITIDOS.contains(mimeType)) {
            inputStream.close()
            return ResultadoValidacionEvidencia.Error(
                "Tipo de archivo no permitido ($mimeType). Solo se aceptan imágenes JPG, PNG o WEBP."
            )
        }

        // 3. Determinar el tamaño en bytes
        var tamanoBytes = 0L
        try {
            contentResolver.query(uriOrigen, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) {
                        tamanoBytes = cursor.getLong(index)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorar error de cursor
        }

        if (tamanoBytes <= 0) {
            try {
                contentResolver.openFileDescriptor(uriOrigen, "r")?.use { pfd ->
                    tamanoBytes = pfd.statSize
                }
            } catch (e: Exception) {
                // Ignorar
            }
        }

        if (tamanoBytes <= 0) {
            inputStream.close()
            return ResultadoValidacionEvidencia.Error("El archivo de la evidencia está vacío o corrupto.")
        }

        if (tamanoBytes > limiteBytes) {
            inputStream.close()
            val tamanoMB = String.format("%.2f", tamanoBytes / (1024.0 * 1024.0))
            val limiteMB = limiteBytes / (1024 * 1024)
            return ResultadoValidacionEvidencia.Error(
                "La imagen seleccionada ($tamanoMB MB) excede el tamaño máximo permitido ($limiteMB MB)."
            )
        }

        // 4. Copiar de forma segura al subdirectorio interno "evidencias/" de la app
        return try {
            val dirEvidencias = EvidenciaStorageUtil.obtenerDirectorioEvidencias(context)
            val nombreArchivo = "evidencia_${System.currentTimeMillis()}.jpg"
            val archivoDestino = File(dirEvidencias, nombreArchivo)

            FileOutputStream(archivoDestino).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Devuelve una URI file:// local directamente accesible de forma nativa por Coil y el sistema interno
            val uriFinal = Uri.fromFile(archivoDestino)

            ResultadoValidacionEvidencia.Exito(
                uriLocalFinal = uriFinal,
                mimeType = mimeType,
                tamanoBytes = tamanoBytes
            )
        } catch (e: Exception) {
            inputStream.close()
            ResultadoValidacionEvidencia.Error("Error al guardar la evidencia en el almacenamiento local: ${e.localizedMessage}")
        }
    }
}
