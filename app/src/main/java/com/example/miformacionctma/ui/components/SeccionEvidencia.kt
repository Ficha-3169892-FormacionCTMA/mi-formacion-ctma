package com.example.miformacionctma.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.util.EvidenciaStorageUtil
import com.example.miformacionctma.data.util.EvidenciaValidador
import com.example.miformacionctma.data.util.ResultadoValidacionEvidencia

/**
 * Componente de UI en Compose para gestionar múltiples evidencias fotográficas (1 a muchos)
 * con soporte diferenciado para Aprendices (subida/gestión) e Instructores (visualización global),
 * incluyendo vista previa en pantalla completa al presionar cualquier imagen.
 */
@Composable
fun SeccionEvidencia(
    evidencias: List<EvidenciaEntity>,
    actividadId: Long,
    esInstructor: Boolean = false,
    onEvidenciaValidaSeleccionada: (Uri, String, Long) -> Unit = { _, _, _ -> },
    onReintentarSubida: (Long) -> Unit = {},
    onEliminarEvidencia: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uriCamaraTemp by remember { mutableStateOf<Uri?>(null) }
    var mensajeErrorValidacion by remember { mutableStateOf<String?>(null) }
    var imagenUrlCompleta by remember { mutableStateOf<Uri?>(null) }

    fun procesarUriSeleccionada(uri: Uri) {
        mensajeErrorValidacion = null
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            procesarUriSeleccionada(uri)
        }
    }

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

    // DIÁLOGO PARA VER IMAGEN EN PANTALLA COMPLETA
    if (imagenUrlCompleta != null) {
        Dialog(onDismissRequest = { imagenUrlCompleta = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vista Completa de Evidencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    AsyncImage(
                        model = imagenUrlCompleta,
                        contentDescription = "Evidencia en pantalla completa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { },
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { imagenUrlCompleta = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
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
                text = if (esInstructor) "Entregas de los Estudiantes (${evidencias.size})" else "Mis Evidencias Fotográficas (${evidencias.size})",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = if (esInstructor) {
                    "Panel de revisión: Evidencias fotográficas subidas por los estudiantes (pulsa cualquier foto para verla en grande)."
                } else {
                    "Puedes adjuntar cuantas fotografías necesites (pulsa cualquier foto para ampliarla)."
                },
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

            // Si es Aprendiz, mostrar botones de subida
            if (!esInstructor) {
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

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Visualización de evidencias
            if (evidencias.isNotEmpty()) {
                if (esInstructor) {
                    val evidenciasPorEstudiante = evidencias.groupBy { it.usuarioId.ifBlank { "Estudiante No Identificado" } }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        evidenciasPorEstudiante.forEach { (estudianteId, listaEvidencias) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Estudiante: $estudianteId",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    listaEvidencias.forEach { evidencia ->
                                        val baseUrlStorage = BuildConfig.SUPABASE_URL.replace("/rest/v1/", "/").trimEnd('/')
                                        val remoteUri = Uri.parse("$baseUrlStorage/storage/v1/object/public/evidencias/evidencia_${evidencia.actividadId}_${evidencia.id}_${evidencia.usuarioId.takeIf { it.isNotBlank() } ?: "general"}.jpg")
                                        val uriFinal = evidencia.localUri.let { Uri.parse(it) } ?: remoteUri

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AsyncImage(
                                                    model = uriFinal,
                                                    contentDescription = "Evidencia de alumno",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(160.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable { imagenUrlCompleta = uriFinal },
                                                    contentScale = ContentScale.Crop
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Estado: ${evidencia.estado.name}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        evidencias.forEach { evidencia ->
                            val baseUrlStorage = BuildConfig.SUPABASE_URL.replace("/rest/v1/", "/").trimEnd('/')
                            val remoteUri = Uri.parse("$baseUrlStorage/storage/v1/object/public/evidencias/evidencia_${evidencia.actividadId}_${evidencia.id}_${evidencia.usuarioId.takeIf { it.isNotBlank() } ?: "general"}.jpg")
                            val uriFinal = evidencia.localUri.let { Uri.parse(it) } ?: remoteUri

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
                                        model = uriFinal,
                                        contentDescription = "Evidencia",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { imagenUrlCompleta = uriFinal },
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val colorEstado = when (evidencia.estado.name) {
                                        "SINCRONIZADA" -> MaterialTheme.colorScheme.primary
                                        "SUBIENDO" -> MaterialTheme.colorScheme.tertiary
                                        "FALLIDA" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    Text(
                                        text = "Estado: ${evidencia.estado.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorEstado
                                    )

                                    if (evidencia.estado.name == "FALLIDA") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { onReintentarSubida(evidencia.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reintentar Subida")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { onEliminarEvidencia(evidencia.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar Evidencia")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = if (esInstructor) "Ningún estudiante ha subido evidencias para esta actividad." else "No hay evidencias adjuntas todavía.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
