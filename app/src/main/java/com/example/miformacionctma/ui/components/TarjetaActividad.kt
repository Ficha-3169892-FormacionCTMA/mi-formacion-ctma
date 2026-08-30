package com.example.miformacionctma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

@Composable
fun TarjetaActividad(
    actividad: ActividadFormativa,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val progresoSeguro = actividad.progreso.coerceIn(0, 100)

    val textoEstado = when {
        progresoSeguro >= 100 -> "Completada"
        progresoSeguro > 0 -> "En proceso"
        else -> "Pendiente"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = actividad.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            if (actividad.descripcion.isNotBlank()) {
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            EstadoActividad(texto = textoEstado)

            Text(
                text = "Progreso: $progresoSeguro%",
                style = MaterialTheme.typography.bodyMedium
            )

            LinearProgressIndicator(
                progress = { progresoSeguro / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prioridad: ${textoPrioridad(actividad.prioridad)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Fecha: ${actividad.fecha}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun textoPrioridad(prioridad: Prioridad): String {
    return when (prioridad) {
        Prioridad.BAJA -> "Baja"
        Prioridad.MEDIA -> "Media"
        Prioridad.ALTA -> "Alta"
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaActividadPreviewNormal() {
    MiFormacionCTMATheme {
        TarjetaActividad(
            actividad = ActividadFormativa(
                id = 1L,
                titulo = "Kotlin básico",
                descripcion = "Repasar funciones y clases",
                fecha = "2026-09-02",
                progreso = 65,
                diasRestantes = 3,
                prioridad = Prioridad.ALTA
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun TarjetaActividadPreviewTituloLargo() {
    MiFormacionCTMATheme {
        TarjetaActividad(
            actividad = ActividadFormativa(
                id = 2L,
                titulo = "Validar títulos extremadamente largos dentro de una tarjeta reutilizable de actividades para Compose",
                descripcion = "Comprobar que el diseño no se rompa con textos extensos",
                fecha = "2026-09-06",
                progreso = 20,
                diasRestantes = 7,
                prioridad = Prioridad.MEDIA
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaActividadPreviewCompletada() {
    MiFormacionCTMATheme {
        TarjetaActividad(
            actividad = ActividadFormativa(
                id = 3L,
                titulo = "Actividad completada",
                descripcion = "Debe mostrar el estado Completada",
                fecha = "2026-08-29",
                progreso = 100,
                diasRestantes = 0,
                prioridad = Prioridad.MEDIA
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaActividadPreviewSinIniciar() {
    MiFormacionCTMATheme {
        TarjetaActividad(
            actividad = ActividadFormativa(
                id = 4L,
                titulo = "Actividad pendiente",
                descripcion = "Debe mostrar el estado Pendiente",
                fecha = "2026-09-09",
                progreso = 0,
                diasRestantes = 10,
                prioridad = Prioridad.BAJA
            )
        )
    }
}
