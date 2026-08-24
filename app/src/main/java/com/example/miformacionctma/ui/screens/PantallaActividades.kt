package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.domain.actividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.components.TarjetaActividad
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

@Composable
fun ContenidoAdaptable(
    actividades: List<ActividadFormativa>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth < 600.dp) {
                PantallaActividades(actividades = actividades)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(actividades, key = { it.id }) { actividad ->
                        TarjetaActividad(actividad = actividad)
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaActividades(
    actividades: List<ActividadFormativa>
) {
    // Estado para rastrear el texto de búsqueda
    var textoBusqueda by remember { mutableStateOf("") }

    // Filtrado dinámico por título o descripción
    val actividadesFiltradas = actividades.filter { actividad ->
        val coincideTitulo = actividad.titulo.contains(textoBusqueda, ignoreCase = true)
        val coincideDescripcion = actividad.descripcion?.contains(textoBusqueda, ignoreCase = true) ?: false
        coincideTitulo || coincideDescripcion
    }

    val urgentes = actividades.count { it.progreso < 100 && it.diasRestantes <= 2 }
    val promedio = if (actividades.isNotEmpty()) actividades.map { it.progreso }.average().toInt() else 0
    val completadas = actividades.count { it.progreso >= 100 }

    val resumen = buildString {
        appendLine("Urgentes: $urgentes")
        appendLine("Promedio: $promedio%")
        appendLine("Completadas: $completadas")
        appendLine("Total actividades: ${actividades.size}")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold { paddingValues ->
            if (actividades.isEmpty()) {
                EstadoVacio(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { SeccionPresentacion(resumen = resumen) }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item { EncabezadoActividades() }

                    // Componente de la Barra de Búsqueda
                    item {
                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            label = { Text("Buscar actividad...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Evaluación de la lista filtrada
                    if (actividadesFiltradas.isEmpty()) {
                        item {
                            Text(
                                text = "No se encontraron coincidencias para \"$textoBusqueda\"",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(actividadesFiltradas, key = { it.id }) { actividad ->
                            TarjetaActividad(actividad = actividad)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    item { SeccionAgile() }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoActividades() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Actividades Formativas",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Consulta tus actividades y revisa su progreso actual.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No hay actividades registradas",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Agrega una actividad para comenzar a organizar tu formación.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(name = "Actividades normales", showBackground = true)
@Composable
private fun PantallaActividadesPreview() {
    MiFormacionCTMATheme {
        PantallaActividades(actividades = actividadesDemo)
    }
}

@Preview(name = "Actividades anchas", showBackground = true, widthDp = 700)
@Composable
private fun PantallaActividadesPreviewAncha() {
    MiFormacionCTMATheme {
        ContenidoAdaptable(actividades = actividadesDemo)
    }
}

@Preview(name = "Estado vacío", showBackground = true)
@Composable
private fun EstadoVacioPreview() {
    MiFormacionCTMATheme {
        EstadoVacio()
    }
}