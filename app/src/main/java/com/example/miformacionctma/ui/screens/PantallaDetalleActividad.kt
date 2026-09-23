package com.example.miformacionctma.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.OperacionUiState
import com.example.miformacionctma.ui.components.cargarMiniatura
import com.example.miformacionctma.ui.components.prepararImagenParaSubir
import kotlinx.coroutines.launch
import java.io.File

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

    val operacionState by viewModel.operacionUiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val procesarUri = { uri: Uri ->
        scope.launch {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = if (mimeType.startsWith("image/")) prepararImagenParaSubir(context, uri) else null

                when {
                    bytes == null -> mensajeUsuario = "Archivo no admitido. Debe ser una imagen válida."
                    bytes.size > 5 * 1024 * 1024 -> mensajeUsuario = "La imagen supera el límite máximo permitido de 5 MB."
                    else -> {
                        mensajeUsuario = "Subiendo archivo a Supabase..."
                        viewModel.subirEvidencia(
                            actividadId = actividadId.toInt(),
                            bytes = bytes,
                            tipoMime = "image/jpeg"
                        ) { _, msj ->
                            mensajeUsuario = msj
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PantallaDetalle", "Error al procesar la imagen", e)
                mensajeUsuario = "Error al procesar el archivo: ${e.localizedMessage}"
            }
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

    // Archivo temporal para la foto de la cámara (carpeta cache declarada en file_paths.xml)
    val tempFileCamara = remember(actividadId) {
        val storageDir = File(context.cacheDir, "images").apply { mkdirs() }
        File(storageDir, "camara_${actividadId}.jpg")
    }

    val cameraUri = remember(actividadId) {
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFileCamara
            )
        } catch (e: Exception) {
            Log.e("PantallaDetalle", "Error al generar FileProvider Uri: ${e.message}", e)
            null
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            procesarUri(cameraUri)
        } else {
            mensajeUsuario = "Acción cancelada. No se tomó ninguna fotografía."
        }
    }

    val abrirCamara = {
        if (cameraUri == null) {
            mensajeUsuario = "Error al inicializar la cámara."
        } else {
            try {
                takePictureLauncher.launch(cameraUri)
            } catch (e: ActivityNotFoundException) {
                mensajeUsuario = "No se encontró una aplicación de cámara en el dispositivo."
            } catch (e: SecurityException) {
                mensajeUsuario = "La app no tiene permiso para usar la cámara."
            }
        }
    }

    // Como el Manifest declara CAMERA, Android exige que el permiso esté concedido antes de abrir la cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            abrirCamara()
        } else {
            mensajeUsuario = "Permiso de cámara denegado. Actívalo en Ajustes para tomar fotos."
        }
    }

    val solicitarCamara = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val abrirGaleria = {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val miniatura by produceState<ImageBitmap?>(initialValue = null, evidenciaState?.uri) {
        value = evidenciaState?.uri?.let { cargarMiniatura(it) }
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

                        if (operacionState is OperacionUiState.EnCurso) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Subiendo imagen a Supabase...")
                            }
                        }

                        val ev = evidenciaState
                        if (ev != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                miniatura?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = "Evidencia de la actividad",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                                val textoEstado = when (ev.estado) {
                                    EvidenciaEntity.ESTADO_SINCRONIZADA -> "Sincronizada con Supabase"
                                    EvidenciaEntity.ESTADO_FALLIDA -> "Guardada en el teléfono, pendiente de subir"
                                    else -> "Subiendo..."
                                }
                                Text(
                                    "Estado: $textoEstado",
                                    color = if (ev.estado == EvidenciaEntity.ESTADO_FALLIDA) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text("Tipo: ${ev.tipo} | Tamaño: ${(ev.tamano / 1024)} KB", style = MaterialTheme.typography.bodySmall)

                                if (ev.estado == EvidenciaEntity.ESTADO_FALLIDA) {
                                    Button(
                                        onClick = {
                                            mensajeUsuario = "Reintentando subida..."
                                            viewModel.sincronizarEvidencias()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Reintentar subida")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = abrirGaleria,
                                        enabled = operacionState !is OperacionUiState.EnCurso,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Galería")
                                    }
                                    OutlinedButton(
                                        onClick = solicitarCamara,
                                        enabled = operacionState !is OperacionUiState.EnCurso,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cámara")
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.eliminarEvidencia(actividadId.toInt())
                                        if (tempFileCamara.exists()) tempFileCamara.delete()
                                        mensajeUsuario = "Evidencia eliminada correctamente."
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Eliminar evidencia")
                                }
                            }
                        } else if (operacionState !is OperacionUiState.EnCurso) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = abrirGaleria,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Galería")
                                }
                                Button(
                                    onClick = solicitarCamara,
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
    if (fecha.isBlank()) return ""
    return try {
        if (fecha.contains("-")) {
            val partes = fecha.split("-")
            if (partes.size == 3) {
                "${partes[2]}/${partes[1]}/${partes[0]}"
            } else fecha
        } else {
            fecha
        }
    } catch (e: Exception) {
        fecha
    }
}