package com.example.miformacionctma.ui.components

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.util.UUID

/**
 * Genera una URI segura usando FileProvider para almacenar la captura de la cámara.
 */
fun newEvidenceUri(context: Context): Uri {
    val dir = File(context.filesDir, "evidencias").apply { mkdirs() }
    val file = File(dir, "evidencia_${UUID.randomUUID()}.jpg").apply {
        createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Componente que expone los botones para seleccionar o tomar una evidencia fotográfica.
 */
@Composable
fun EvidenciaSelectorSection(
    onEvidenciaSelected: (Uri) -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para el Photo Picker del sistema
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onEvidenciaSelected(uri)
        } else {
            onCancelled()
        }
    }

    // Launcher para capturar foto con la app de cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            onEvidenciaSelected(uri)
        } else {
            // Si la persona canceló la cámara, eliminamos el archivo temporal generado
            uri?.let { context.contentResolver.delete(it, null, null) }
            onCancelled()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Botón Photo Picker
        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Text("Elegir de Galería")
        }

        // Botón Toma de Foto con Cámara
        Button(
            onClick = {
                val uri = newEvidenceUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
        ) {
            Text("Tomar Foto")
        }
    }
}

/**
 * Muestra la vista previa de la evidencia, metadatos y botones de control.
 */
@Composable
fun EvidenciaPreviewCard(
    imageUri: Uri,
    mimeType: String,
    sizeBytes: Long,
    estado: String,
    onReemplazar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vista previa de la imagen usando Coil
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Vista previa de evidencia",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Evidencia Fotográfica",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tipo: $mimeType",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Tamaño: ${sizeBytes / 1024} KB",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Estado: $estado",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (estado) {
                            "SINCRONIZADA" -> MaterialTheme.colorScheme.primary
                            "FALLIDA" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onReemplazar) {
                    Text("Reemplazar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onEliminar,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}