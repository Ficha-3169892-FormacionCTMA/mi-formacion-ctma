package com.example.miformacionctma.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.miformacionctma.data.util.EvidenciaStorageUtil
import com.example.miformacionctma.data.util.EvidenciaValidador
import com.example.miformacionctma.data.util.ResultadoValidacionEvidencia

/**
 * Componente de UI en Compose para gestionar la selección, captura, validación,
 * vista previa, reintento y eliminación de la evidencia fotográfica de una actividad.
 */
@Composable
fun SeccionEvidencia(
    evidenciaUri: Uri?,
    estadoSincronizacion: String? = null,
    onEvidenciaValidaSeleccionada: (Uri, String, Long) -> Unit,
    onReintentarSubida: () -> Unit = {},
    onEliminarEvidencia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uriCamaraTemp by remember { mutableStateOf<Uri?>(null) }
    var mensajeErrorValidacion by remember { mutableStateOf<String?>(null) }
    var imagenCargadaConExito by remember(evidenciaUri) { mutableStateOf(true) }

    fun procesarUriSeleccionada(uri: Uri) {
        mensajeErrorValidacion = null
        imagenCargadaConExito = true
        when (val resultado = EvidenciaValidador.validarYProcesarEvidencia(context, uri)) {
            is ResultadoValidacionEvidencia.Exito -> {
                onEvidenciaValidaSeleccionada(
                    resultado.uriLocalFinal,
                    resultado.mimeType,
                    resultado.tamanoBytes
                )
            }
            is ResultadoValidacionEvidencia.Error -> {
                mensajeErrorValidacion = resultado.mensaje
            }
        }
    }

    // 1. Selector Nativo Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            procesarUriSeleccionada(uri)
        }
    }

    // 2. Captura con la cámara nativa
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        if (exito && uriCamaraTemp != null) {
            procesarUriSeleccionada(uriCamaraTemp!!)
        } else {
            EvidenciaStorageUtil.eliminarArchivoSiExiste(context, uriCamaraTemp)
        }
        uriCamaraTemp = null
    }

    fun abrirCamara() {
        mensajeErrorValidacion = null
        try {
            val uri = EvidenciaStorageUtil.crearUriFotoTemporal(context)
            uriCamaraTemp = uri
            camaraLauncher.launch(uri)
        } catch (e: Exception) {
            uriCamaraTemp = null
            mensajeErrorValidacion = "No se pudo iniciar la cámara: ${e.localizedMessage}"
        }
    }

    fun abrirGaleria() {
        mensajeErrorValidacion = null
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Evidencia de la Actividad",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Adjunta una fotografía o imagen como constancia del trabajo realizado en esta actividad formativa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (mensajeErrorValidacion != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = mensajeErrorValidacion!!,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (evidenciaUri != null && imagenCargadaConExito) {
                // Vista Previa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = evidenciaUri,
                            contentDescription = "Vista previa de la evidencia",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            onError = {
                                imagenCargadaConExito = false
                            }
                        )

                        if (estadoSincronizacion != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val colorEstado = when (estadoSincronizacion) {
                                "SINCRONIZADA" -> MaterialTheme.colorScheme.primary
                                "SUBIENDO" -> MaterialTheme.colorScheme.tertiary
                                "FALLIDA" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                text = "Estado de Sincronización: $estadoSincronizacion",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorEstado
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón de Reintentar si está en estado FALLIDA
                        if (estadoSincronizacion == "FALLIDA") {
                            Button(
                                onClick = onReintentarSubida,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reintentar Subida a la Nube")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Botones para "Reemplazar" y "Eliminar"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { abrirGaleria() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reemplazar")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onEliminarEvidencia,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            } else {
                // Botones para Seleccionar o Tomar Foto cuando no hay evidencia previa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { abrirGaleria() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería")
                    }

                    Button(
                        onClick = { abrirCamara() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }
                }
            }
        }
    }
}
