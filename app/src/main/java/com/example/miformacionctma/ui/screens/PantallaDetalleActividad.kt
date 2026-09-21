package com.example.miformacionctma.ui.screens

import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import java.io.File

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: Long,
    actividades: List<ActividadFormativa>,
    viewModel: ActividadViewModel,
    onVolver: () -> Unit,
    onEliminarClick: (Long) -> Unit = {},
    onEditarClick: (Long) -> Unit = {}
) {
    val actividad = actividades.find { it.id == actividadId }
    val context = LocalContext.current
    var mensajeUsuario by remember { mutableStateOf<String?>(null) }
    var recordatoriosActivados by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val evidenciaState by viewModel.observarEvidencia(actividadId.toInt())
        .collectAsStateWithLifecycle(initialValue = null)

    val procesarUri = { uri: Uri ->
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            if (!mimeType.startsWith("image/")) {
                mensajeUsuario = "Archivo no admitido. Debe ser una imagen válida."
            } else {
                var fileSize = 0L
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }

                if (fileSize > 5 * 1024 * 1024) {
                    mensajeUsuario = "La imagen supera el límite máximo permitido de 5 MB."
                } else {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    inputStream?.close()

                    if (bytes.isNotEmpty()) {
                        val nombreArchivo = "evidencia_${actividadId}_${System.currentTimeMillis()}.jpg"
                        viewModel.guardarYSubirEvidencia(
                            actividadId = actividadId.toInt(),
                            uriStr = uri.toString(),
                            tipoMime = mimeType,
                            tamano = fileSize,
                            bytes = bytes,
                            nombreArchivo = nombreArchivo
                        )
                        mensajeUsuario = "Evidencia cargada localmente correctamente."
                    } else {
                        mensajeUsuario = "No se pudo leer el archivo seleccionado."
                    }
                }
            }
        } catch (e: Exception) {
            mensajeUsuario = "Error al procesar el archivo: ${e.localizedMessage}"
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            procesarUri(uri)
        } else {
            mensajeUsuario = "Acción cancelada. No se seleccionó ninguna imagen."
        }
    }

    val tempFile = remember {
        File(context.cacheDir, "evidencias/camara_${actividadId}.jpg").apply {
            parentFile?.mkdirs()
        }
    }

    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            procesarUri(cameraUri)
        } else {
            mensajeUsuario = "Acción cancelada. No se tomó ninguna fotografía."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        recordatoriosActivados = granted
        if (!granted) {
            mensajeUsuario = "Permiso de notificaciones denegado."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Actividad") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (actividad != null) {
                val textoPrioridad = when (actividad.prioridad) {
                    Prioridad.BAJA -> "Baja"
                    Prioridad.MEDIA -> "Media"
                    Prioridad.ALTA -> "Alta"
                }

                val fechaFormateada = formatearFecha(actividad.fecha)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = actividad.titulo,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Descripción:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = actividad.descripcion.ifBlank { "Sin descripción disponible." },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                        Text(
                            text = "Fecha límite: $fechaFormateada",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Días restantes: ${actividad.diasRestantes}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Prioridad: $textoPrioridad",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Progreso: ${actividad.progreso}%",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        LinearProgressIndicator(
                            progress = { actividad.progreso / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Text("Evidencia de la Actividad", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Adjunta una imagen como prueba del desarrollo de la actividad formativa asignada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (evidenciaState != null) {
                            val ev = evidenciaState!!
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Estado: ${ev.estado}", color = MaterialTheme.colorScheme.primary)
                                Text("Tipo: ${ev.tipo} | Tamaño: ${(ev.tamano / 1024)} KB", style = MaterialTheme.typography.bodySmall)

                                if (ev.estado == "FALLIDA") {
                                    Button(
                                        onClick = {
                                            try {
                                                val uri = Uri.parse(ev.uri)
                                                val inputStream = context.contentResolver.openInputStream(uri)
                                                val bytes = inputStream?.readBytes() ?: byteArrayOf()
                                                inputStream?.close()
                                                val nombreArchivo = "evidencia_${actividadId}_retry.jpg"
                                                viewModel.guardarYSubirEvidencia(actividadId.toInt(), ev.uri, ev.tipo, ev.tamano, bytes, nombreArchivo)
                                            } catch (e: Exception) {
                                                mensajeUsuario = "No se pudo reintentar: archivo no disponible."
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Reintentar Subida")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reemplazar")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.eliminarEvidencia(actividadId.toInt())
                                            if (tempFile.exists()) tempFile.delete()
                                            mensajeUsuario = "Evidencia eliminada correctamente."
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                        Text("Galería")
                                }
                                Button(
                                    onClick = {
                                        takePictureLauncher.launch(cameraUri)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cámara")
                                }
                            }
                        }

                        mensajeUsuario?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Recordatorios de Actividad", style = MaterialTheme.typography.bodyLarge)
                                Text("Recibe alertas antes de la fecha límite.", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = recordatoriosActivados,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            recordatoriosActivados = true
                                        }
                                    } else {
                                        recordatoriosActivados = false
                                    }
                                }
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEditarClick(actividad.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Editar Actividad")
                        }

                        Button(
                            onClick = { onEliminarClick(actividad.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Eliminar Actividad")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Actividad no encontrada (ID: $actividadId)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onVolver) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

private fun formatearFecha(fecha: String): String {
    val partes = fecha.split("-")
    return if (partes.size == 3) {
        "${partes[2]}/${partes[1]}/${partes[0]}"
    } else {
        fecha
    }
}
