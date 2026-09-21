package com.example.miformacionctma.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.miformacionctma.model.EstadoEvidencia
import com.example.miformacionctma.model.Evidencia
import com.example.miformacionctma.ui.viewmodel.EvidenciaUiState
import com.example.miformacionctma.ui.viewmodel.EvidenciaViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciaScreen(
    actividadId: Long,
    viewModel: EvidenciaViewModel,
    actividadesRelacionadas: List<com.example.miformacionctma.model.ActividadFormativa> = emptyList(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorEvent by viewModel.errorEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorEvent) {
        errorEvent?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(actividadId, actividadesRelacionadas) {
        val ids = actividadesRelacionadas.map { it.id }
        viewModel.cargarEvidencias(actividadId, ids)
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { viewModel.guardarEvidencia(actividadId, it, "Captura de cámara") }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.guardarEvidencia(actividadId, it, "Selección de galería") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evidencias") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = {
                        val dir = File(context.filesDir, "evidencias")
                        if (!dir.exists()) dir.mkdirs()
                        val file = File(dir, "temp_capture_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        tempUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                }
                FloatingActionButton(
                    onClick = {
                        pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is EvidenciaUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EvidenciaUiState.Contenido -> {
                    if (state.evidencias.isEmpty()) {
                        Text("No hay evidencias", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.evidencias) { evidencia ->
                                val nombreEstudiante = remember(actividadesRelacionadas, evidencia.actividadId) {
                                    actividadesRelacionadas.find { it.id == evidencia.actividadId }?.estudianteNombre
                                }
                                EvidenciaItem(
                                    evidencia = evidencia,
                                    estudianteNombre = nombreEstudiante,
                                    onSync = { viewModel.sincronizar(evidencia.id) },
                                    onDelete = { viewModel.eliminar(evidencia.id) }
                                )
                            }
                        }
                    }
                }
                is EvidenciaUiState.Error -> {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun EvidenciaItem(
    evidencia: Evidencia,
    estudianteNombre: String? = null,
    onSync: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val model: Any = remember(evidencia.uriLocal, evidencia.remoteUrl) {
                val fileLocal = if (evidencia.uriLocal.isNotBlank()) File(evidencia.uriLocal) else null
                if (fileLocal != null && fileLocal.exists()) {
                    fileLocal
                } else {
                    evidencia.remoteUrl ?: ""
                }
            }

            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            if (!estudianteNombre.isNullOrBlank()) {
                Text(
                    text = "Subido por: $estudianteNombre",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(evidencia.nombreArchivo, style = MaterialTheme.typography.titleMedium)
            Text("Estado: ${evidencia.estado.name}", style = MaterialTheme.typography.bodySmall)
            if (evidencia.finalidad != null) {
                Text(evidencia.finalidad, style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (evidencia.estado == EstadoEvidencia.LOCAL || evidencia.estado == EstadoEvidencia.FALLIDA) {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
