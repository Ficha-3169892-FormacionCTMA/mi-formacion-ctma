package com.example.miformacionctma.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

@Composable
fun TarjetaActividad(
    actividad: ActividadFormativa,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val progresoSeguro = actividad.progreso.coerceIn(0, 100)

    val textoEstado = when {
        progresoSeguro >= 100 -> "COMPLETADA"
        progresoSeguro > 0 -> "EN PROCESO"
        else -> "PENDIENTE"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = actividad.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            if (actividad.descripcion.isNotBlank()) {
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 2
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                EstadoActividad(texto = textoEstado)
                if (actividad.estudianteNombre != null) {
                    Text(
                        text = actividad.estudianteNombre,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progresoSeguro / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prioridad: ${actividad.prioridad.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = actividad.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
