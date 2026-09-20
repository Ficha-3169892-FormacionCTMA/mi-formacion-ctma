package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.Rol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: Long,
    actividades: List<ActividadFormativa>,
    userRole: Rol = Rol.ESTUDIANTE,
    onVolver: () -> Unit,
    onEditarClick: (Long) -> Unit = {},
    onEliminarClick: (Long) -> Unit = {},
    onEvidenciaClick: (Long) -> Unit = {}
) {
    val actividad = actividades.find { it.id == actividadId }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (actividad != null && userRole == Rol.INSTRUCTOR) {
                        IconButton(onClick = { onEditarClick(actividad.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (actividad != null) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = actividad.titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Descripción", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            text = actividad.descripcion.ifBlank { "Sin descripción detallada." },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn("FECHA LÍMITE", actividad.fecha)
                        InfoColumn("PRIORIDAD", actividad.prioridad.name)
                    }
                    
                    if (actividad.estudianteNombre != null) {
                        InfoColumn("ESTUDIANTE ASIGNADO", actividad.estudianteNombre)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PROGRESO ACTUAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${actividad.progreso}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        LinearProgressIndicator(
                            progress = { actividad.progreso / 100f },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onEvidenciaClick(actividad.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EVIDENCIAS FOTOGRÁFICAS", fontWeight = FontWeight.Bold)
                    }
                }

                if (mostrarConfirmarEliminar) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmarEliminar = false },
                        title = { Text("Eliminar Registro") },
                        text = { Text("¿Confirma que desea eliminar permanentemente esta actividad del sistema?") },
                        confirmButton = {
                            TextButton(onClick = { mostrarConfirmarEliminar = false; onEliminarClick(actividad.id) }) {
                                Text("SÍ, ELIMINAR", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                                Text("CANCELAR")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
