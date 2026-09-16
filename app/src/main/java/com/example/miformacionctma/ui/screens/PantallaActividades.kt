package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

@Composable
fun ContenidoAdaptable(
    actividades: List<ActividadFormativa>,
    modifier: Modifier = Modifier,
    queryBusqueda: String = "",
    onQueryChange: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth < 600.dp) {
                PantallaActividades(
                    actividades = actividades,
                    queryBusqueda = queryBusqueda,
                    onQueryChange = onQueryChange,
                    snackbarHostState = snackbarHostState,
                    onActividadClick = onActividadClick,
                    onCrearClick = onCrearClick
                )
            } else {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(padding)
                    ) {
                        items(actividades, key = { it.id }) { actividad ->
                            TarjetaActividad(
                                actividad = actividad,
                                onClick = { onActividadClick(actividad.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaActividades(
    actividades: List<ActividadFormativa>,
    modifier: Modifier = Modifier,
    queryBusqueda: String = "",
    onQueryChange: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {}
) {
    val urgentes = ReglasActividad.actividadesUrgentes(actividades).size
    val promedio = ReglasActividad.promedioProgreso(actividades).toInt()
    val completadas = actividades.count { it.progreso >= 100 }

    val resumen = buildString {
        appendLine("Urgentes: $urgentes")
        appendLine("Promedio: $promedio%")
        appendLine("Completadas: $completadas")
        appendLine("Total actividades: ${actividades.size}")
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = onCrearClick) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        ) { paddingValues ->
            if (actividades.isEmpty() && queryBusqueda.isBlank()) {
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

                    item {
                        OutlinedTextField(
                            value = queryBusqueda,
                            onValueChange = onQueryChange,
                            label = { Text("Buscar actividad...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (actividades.isEmpty()) {
                        item {
                            Text(
                                text = "No se encontraron coincidencias para \"$queryBusqueda\"",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(actividades, key = { it.id }) { actividad ->
                            TarjetaActividad(
                                actividad = actividad,
                                onClick = { onActividadClick(actividad.id) }
                            )
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
fun TarjetaActividad(
    actividad: ActividadFormativa,
    onClick: () -> Unit
) {
    val fechaFormateada = remember(actividad.fecha) {
        val partes = actividad.fecha.split("-", "/")
        if (partes.size == 3) {
            "${partes[0]}/${partes[1]}/${partes[2]}"
        } else {
            actividad.fecha
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = actividad.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = actividad.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Progreso: ${actividad.progreso}%",
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = { actividad.progreso / 100f },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prioridad: ${actividad.prioridad.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Fecha: $fechaFormateada",
                    style = MaterialTheme.typography.labelSmall
                )
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
        PantallaActividades(actividades = ActividadesDemo.listaInicial)
    }
}

@Preview(name = "Actividades anchas", showBackground = true, widthDp = 700)
@Composable
private fun PantallaActividadesPreviewAncha() {
    MiFormacionCTMATheme {
        ContenidoAdaptable(actividades = ActividadesDemo.listaInicial)
    }
}

@Preview(name = "Estado vacío", showBackground = true)
@Composable
private fun EstadoVacioPreview() {
    MiFormacionCTMATheme {
        EstadoVacio()
    }
}