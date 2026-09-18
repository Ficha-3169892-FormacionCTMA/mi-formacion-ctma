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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.components.TarjetaActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel

@Composable
fun PantallaActividadesRoute(
    viewModel: ActividadesViewModel,
    onActividadClick: (Long) -> Unit,
    onCrearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoBusqueda by viewModel.textoBusqueda.collectAsStateWithLifecycle()
    val ordenarPorPrioridad by viewModel.ordenarPorPrioridad.collectAsStateWithLifecycle()

    ContenidoAdaptable(
        uiState = uiState,
        textoBusqueda = textoBusqueda,
        onTextoBusquedaChange = viewModel::actualizarBusqueda,
        ordenarPorPrioridad = ordenarPorPrioridad,
        onOrdenPorPrioridadChange = viewModel::guardarOrdenPorPrioridad,
        onActividadClick = onActividadClick,
        onCrearClick = onCrearClick,
        onReintentarClick = { viewModel.actualizarBusqueda(textoBusqueda) },
        modifier = modifier
    )
}

@Composable
fun ContenidoAdaptable(
    uiState: ListadoUiState,
    modifier: Modifier = Modifier,
    textoBusqueda: String = "",
    onTextoBusquedaChange: (String) -> Unit = {},
    ordenarPorPrioridad: Boolean = false,
    onOrdenPorPrioridadChange: (Boolean) -> Unit = {},
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onReintentarClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth < 600.dp) {
                PantallaActividadesScreen(
                    uiState = uiState,
                    textoBusqueda = textoBusqueda,
                    onTextoBusquedaChange = onTextoBusquedaChange,
                    ordenarPorPrioridad = ordenarPorPrioridad,
                    onOrdenPorPrioridadChange = onOrdenPorPrioridadChange,
                    onActividadClick = onActividadClick,
                    onCrearClick = onCrearClick,
                    onReintentarClick = onReintentarClick
                )
            } else {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = onCrearClick) {
                            Text("+", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                ) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = onTextoBusquedaChange,
                            label = { Text("Buscar actividad...") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            singleLine = true
                        )

                        FilterChip(
                            selected = ordenarPorPrioridad,
                            onClick = { onOrdenPorPrioridadChange(!ordenarPorPrioridad) },
                            label = { Text("Ordenar por prioridad") },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        when (uiState) {
                            is ListadoUiState.Cargando -> EstadoCargando()
                            is ListadoUiState.Vacio -> EstadoVacio(onCrearClick = onCrearClick)
                            is ListadoUiState.Error -> EstadoError(mensaje = uiState.mensaje, onReintentar = onReintentarClick)
                            is ListadoUiState.Contenido -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.actividades, key = { it.id }) { actividad ->
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
        }
    }
}

@Composable
fun PantallaActividadesScreen(
    uiState: ListadoUiState,
    textoBusqueda: String,
    onTextoBusquedaChange: (String) -> Unit,
    ordenarPorPrioridad: Boolean,
    onOrdenPorPrioridadChange: (Boolean) -> Unit,
    onActividadClick: (Long) -> Unit,
    onCrearClick: () -> Unit,
    onReintentarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = onCrearClick) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (uiState) {
                    is ListadoUiState.Cargando -> {
                        item { EstadoCargando() }
                    }
                    is ListadoUiState.Vacio -> {
                        item { EstadoVacio(onCrearClick = onCrearClick) }
                    }
                    is ListadoUiState.Error -> {
                        item {
                            EstadoError(
                                mensaje = uiState.mensaje,
                                onReintentar = onReintentarClick
                            )
                        }
                    }
                    is ListadoUiState.Contenido -> {
                        val actividades = uiState.actividades
                        val urgentes = ReglasActividad.actividadesUrgentes(actividades).size
                        val promedio = ReglasActividad.promedioProgreso(actividades).toInt()
                        val completadas = actividades.count { it.completada }

                        val resumen = buildString {
                            appendLine("Urgentes: $urgentes")
                            appendLine("Promedio: $promedio%")
                            appendLine("Completadas: $completadas")
                            appendLine("Total actividades: ${actividades.size}")
                        }

                        item { SeccionPresentacion(resumen = resumen) }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                        item { EncabezadoActividades() }

                        item {
                            FilterChip(
                                selected = ordenarPorPrioridad,
                                onClick = {
                                    onOrdenPorPrioridadChange(!ordenarPorPrioridad)
                                },
                                label = {
                                    Text("Ordenar por prioridad")
                                }
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = textoBusqueda,
                                onValueChange = onTextoBusquedaChange,
                                label = { Text("Buscar actividad...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        items(actividades, key = { it.id }) { actividad ->
                            TarjetaActividad(
                                actividad = actividad,
                                onClick = { onActividadClick(actividad.id) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
                item { SeccionAgile() }
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
private fun EstadoCargando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando actividades...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EstadoVacio(
    onCrearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No hay actividades registradas",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Agrega una actividad para comenzar a organizar tu formación.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onCrearClick) {
                Text("Crear Actividad")
            }
        }
    }
}

@Composable
private fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}

@Preview(name = "Actividades normales", showBackground = true)
@Composable
private fun PantallaActividadesPreview() {
    MiFormacionCTMATheme {
        PantallaActividadesScreen(
            uiState = ListadoUiState.Contenido(ActividadesDemo.listaInicial),
            textoBusqueda = "",
            onTextoBusquedaChange = {},
            ordenarPorPrioridad = false,
            onOrdenPorPrioridadChange = {},
            onActividadClick = {},
            onCrearClick = {},
            onReintentarClick = {}
        )
    }
}
