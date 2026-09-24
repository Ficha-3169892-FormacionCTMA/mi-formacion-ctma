package com.example.miformacionctma.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utilidad para gestionar la creación y limpieza de archivos temporales de evidencias
 * limitados al subdirectorio interno "evidencias/".
 */
object EvidenciaStorageUtil {

    private const val DIRECTORIO_EVIDENCIAS = "evidencias"

    /**
     * Obtiene el subdirectorio interno dedicado a las evidencias.
     */
    fun obtenerDirectorioEvidencias(context: Context): File {
        val dir = File(context.filesDir, DIRECTORIO_EVIDENCIAS)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Genera un archivo temporal dentro del subdirectorio "evidencias/" y devuelve su content URI
     * mediante FileProvider.
     * Prohíbe estrictamente la exposición de file URIs a aplicaciones externas.
     */
    fun crearUriFotoTemporal(context: Context): Uri {
        val directorio = obtenerDirectorioEvidencias(context)
        val nombreArchivo = "evidencia_temp_${System.currentTimeMillis()}.jpg"
        val archivo = File(directorio, nombreArchivo)

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, archivo)

        require(uri.scheme == "content") {
            "Error de seguridad: La URI generada debe ser un content URI, no un file URI."
        }

        return uri
    }

    /**
     * Limpia o elimina un archivo temporal si existe o si está vacío (por ejemplo, cuando el usuario
     * cancela la toma de fotografía con la cámara).
     */
    fun eliminarArchivoSiExiste(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        return try {
            val filasBorradas = context.contentResolver.delete(uri, null, null)
            if (filasBorradas > 0) return true

            // Intento secundario directo en almacenamiento interno si contentResolver no lo elimina
            val path = uri.path ?: return false
            val directorio = obtenerDirectorioEvidencias(context)
            val nombreArchivo = path.substringAfterLast('/')
            val archivo = File(directorio, nombreArchivo)
            if (archivo.exists()) {
                archivo.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
