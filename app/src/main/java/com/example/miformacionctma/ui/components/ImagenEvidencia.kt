package com.example.miformacionctma.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

private const val LADO_MAXIMO_SUBIDA = 1600
private const val CALIDAD_JPEG = 85

/**
 * Lee la imagen elegida (cámara o galería), la reduce a máximo 1600 px, corrige la rotación
 * y la comprime a JPEG. Así las fotos de la cámara no superan el límite de 5 MB.
 * Devuelve null si el archivo no es una imagen válida.
 */
suspend fun prepararImagenParaSubir(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return@withContext null

    val opciones = BitmapFactory.Options().apply {
        inSampleSize = calcularMuestreo(limites.outWidth, limites.outHeight, LADO_MAXIMO_SUBIDA)
    }
    val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        ?: return@withContext null

    val grados = resolver.openInputStream(uri)?.use { entrada ->
        when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    val rotada = if (grados != 0f) {
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, Matrix().apply { postRotate(grados) }, true)
    } else {
        original
    }

    ByteArrayOutputStream().use { salida ->
        rotada.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        salida.toByteArray()
    }
}

// Carga una miniatura desde el archivo guardado en el teléfono
suspend fun cargarMiniatura(ruta: String, ladoMaximo: Int = 1024): ImageBitmap? = withContext(Dispatchers.IO) {
    val archivo = File(ruta)
    if (!archivo.exists()) return@withContext null

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    val opciones = BitmapFactory.Options().apply {
        inSampleSize = calcularMuestreo(limites.outWidth, limites.outHeight, ladoMaximo)
    }
    BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
}

private fun calcularMuestreo(ancho: Int, alto: Int, ladoMaximo: Int): Int {
    var muestreo = 1
    while (maxOf(ancho, alto) / (muestreo * 2) >= ladoMaximo) muestreo *= 2
    return muestreo
}
