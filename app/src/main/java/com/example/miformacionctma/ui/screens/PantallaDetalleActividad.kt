package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: Long,
    actividades: List<ActividadFormativa>,
    onVolver: () -> Unit,
    onEditarClick: (Long) -> Unit = {},
    onEliminarClick: (Long) -> Unit = {}
) {
    val actividad = actividades.find { it.id == actividadId }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

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
                },
                actions = {
                    if (actividad != null) {
                        IconButton(onClick = { onEditarClick(actividad.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Actividad"
                            )
                        }
                        IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Actividad"
                            )
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
                .padding(16.dp)
        ) {
            if (actividad != null) {
                val textoPrioridad = when (actividad.prioridad) {
                    Prioridad.BAJA -> "Baja"
                    Prioridad.MEDIA -> "Media"
                    Prioridad.ALTA -> "Alta"
                }

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
                }

                if (mostrarConfirmarEliminar) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmarEliminar = false },
                        title = { Text("Eliminar Actividad") },
                        text = { Text("¿Estás seguro de que deseas eliminar esta actividad? Esta acción no se puede deshacer.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarConfirmarEliminar = false
                                    onEliminarClick(actividad.id)
                                }
                            ) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
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
