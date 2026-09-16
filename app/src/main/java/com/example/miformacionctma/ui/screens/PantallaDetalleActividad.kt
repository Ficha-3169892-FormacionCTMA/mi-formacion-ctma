package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleActividad(
    actividadId: Long,
    actividades: List<ActividadFormativa>,
    onVolver: () -> Unit,
    onEliminarClick: (Long) -> Unit = {},
    onEditarClick: (Long) -> Unit = {}
) {
    val actividad = actividades.find { it.id == actividadId }

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
                    modifier = Modifier.fillMaxSize(),
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
                    }

                    // Botones de acción al final de la pantalla
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