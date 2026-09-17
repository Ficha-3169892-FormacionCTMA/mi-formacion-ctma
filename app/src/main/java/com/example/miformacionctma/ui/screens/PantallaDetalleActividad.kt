package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: Long,
    actividades: List<ActividadFormativa>,
    competenciaNombre: String?,
    onVolver: () -> Unit,
    onEliminar: (ActividadFormativa) -> Unit
) {
    val actividad = actividades.find { it.id == actividadId }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

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

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = actividad.titulo,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )

                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = actividad.descripcion.ifBlank {
                            "Sin descripción disponible."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )

                    Text(
                        text = "Competencia",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = competenciaNombre ?: "Sin competencia",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )

                    Text(
                        text = "Información de la actividad",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha límite",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = actividad.fecha,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = ReglasActividad.estadoActividad(actividad),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prioridad",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = textoPrioridad,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    }

                    Text(
                        text = "Progreso: ${actividad.progreso}%",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    LinearProgressIndicator(
                        progress = { actividad.progreso / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            mostrarDialogoEliminar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar actividad")
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

    if (mostrarDialogoEliminar && actividad != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEliminar = false
            },
            title = {
                Text("Eliminar actividad")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar \"${actividad.titulo}\"? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            mostrarDialogoEliminar = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onEliminar(actividad)
                            mostrarDialogoEliminar = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }
                }
            },
            dismissButton = {}
        )
    }
}
