package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa

@Composable
fun PantallaDetalleActividad(
    actividadId: Long?,
    actividades: List<ActividadFormativa>,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actividad = actividades.find { it.id == actividadId }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (actividad != null) {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = actividad.descripcion ?: "Sin descripción",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Progreso: ${actividad.progreso}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Actividad no encontrada (ID: $actividadId)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(onClick = onVolver) {
                Text("Volver")
            }
        }
    }
}