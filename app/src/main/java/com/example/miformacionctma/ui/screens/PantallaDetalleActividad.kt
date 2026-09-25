package com.example.miformacionctma.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.miformacionctma.data.local.crearDatabase
import com.example.miformacionctma.data.repository.DefaultEvidenciaRepository
import com.example.miformacionctma.data.remote.EvidenciasApi
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: String,
    actividades: List<ActividadFormativa>,
    viewModel: ActividadesViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    val esInstructor by viewModel.esInstructor.collectAsStateWithLifecycle()

    val database = remember { crearDatabase(context) }
    val evidenciaDao = database.evidenciaDao()
    val evidenciasApi = Retrofit.Builder().baseUrl("https://placeholder.url/").build().create(EvidenciasApi::class.java)
    val evidenciaRepository = remember { DefaultEvidenciaRepository(evidenciasApi, evidenciaDao) }

    val evidenciasList by evidenciaRepository.observeEvidencias(actividadId).collectAsStateWithLifecycle(initialValue = emptyList())

    val evidenciaActual = evidenciasList.lastOrNull()
    val evidenciaUri = evidenciaActual?.let { Uri.parse(it.localUri) }

    val actividad = actividades.find { it.id == actividadId }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val archivoInterno = File(context.filesDir, "evidencia_${actividadId}_${System.currentTimeMillis()}.jpg")

                        inputStream?.use { input ->
                            archivoInterno.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        val uriPersistente = Uri.fromFile(archivoInterno)
                        val bytesFoto = archivoInterno.readBytes()

                        evidenciaRepository.guardarEvidenciaLocal(
                            actividadId = actividadId,
                            uri = uriPersistente,
                            resolver = context.contentResolver
                        )

                        val nombreArchivo = "evidencia_${System.currentTimeMillis()}.jpg"
                        viewModel.subirEvidencia(
                            actividadId = actividadId,
                            bytesFoto = bytesFoto,
                            nombreArchivo = nombreArchivo
                        )
                    } catch (e: Exception) {
                        // Manejo de error al procesar el archivo
                    }
                }
            }
        }
    )

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

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sección Superior con Scroll (Detalles y la imagen en el espacio del cuadro amarillo)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            text = "Fecha límite: ${actividad.fecha}",
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

                        // 🟡 LA FOTO APARECE AQUÍ EN EL ESPACIO DEL CUADRO AMARILLO
                        if (evidenciaUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = evidenciaUri,
                                contentDescription = "Evidencia en cuadro amarillo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            )
                        }
                    }

                    // Sección Inferior Fija (Título y botones abajo)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                        Text(
                            text = "Evidencia Fotográfica",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (evidenciaUri == null) "Adjuntar Evidencia" else "Reemplazar Evidencia")
                        }

                        if (esInstructor) {
                            Button(
                                onClick = {
                                    viewModel.eliminar(actividadId)
                                    onVolver()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Eliminar Actividad", color = MaterialTheme.colorScheme.onError)
                            }
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