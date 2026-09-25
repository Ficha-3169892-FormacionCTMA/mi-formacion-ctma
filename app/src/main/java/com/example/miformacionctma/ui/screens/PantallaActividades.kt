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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.components.TarjetaActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

@Composable
fun ContenidoAdaptable(
    uiState: ListadoUiState,
    modifier: Modifier = Modifier,
    textoBusqueda: String = "",
    onBusquedaChange: (String) -> Unit = {},
    onActividadClick: (String) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onReintentar: () -> Unit = {},
    esInstructor: Boolean = true,
    onCerrarSesion: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth < 600.dp) {
                PantallaActividades(
                    uiState = uiState,
                    textoBusqueda = textoBusqueda,
                    onBusquedaChange = onBusquedaChange,
                    onActividadClick = onActividadClick,
                    onCrearClick = onCrearClick,
                    onReintentar = onReintentar,
                    esInstructor = esInstructor,
                    onCerrarSesion = onCerrarSesion
                )
            } else {
                when (uiState) {
                    ListadoUiState.Cargando -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.semantics { contentDescription = "Consultando actividades" })
                        }
                    }
                    ListadoUiState.Vacio -> {
                        if (esInstructor) {
                            EstadoVacio(onCrearClick = onCrearClick)
                        } else {
                            EstadoVacioAprendiz()
                        }
                    }
                    is ListadoUiState.Error -> {
                        Column(
                            Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(uiState.mensaje, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onReintentar) { Text("Reintentar") }
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividades(
    uiState: ListadoUiState,
    textoBusqueda: String = "",
    onBusquedaChange: (String) -> Unit = {},
    onActividadClick: (String) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onReintentar: () -> Unit = {},
    esInstructor: Boolean = true,
    onCerrarSesion: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (esInstructor) "Panel Instructor" else "Panel Aprendiz") },
                    actions = {
                        IconButton(onClick = onCerrarSesion) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar Sesión"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (esInstructor) {
                    FloatingActionButton(onClick = onCrearClick) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        ) { paddingValues ->
            when (uiState) {
                ListadoUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(Modifier.semantics { contentDescription = "Consultando actividades" })
                            Spacer(Modifier.height(8.dp))
                            Text("Consultando actividades...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                ListadoUiState.Vacio -> {
                    if (esInstructor) {
                        EstadoVacio(modifier = Modifier.padding(paddingValues), onCrearClick = onCrearClick)
                    } else {
                        EstadoVacioAprendiz(modifier = Modifier.padding(paddingValues))
                    }
                }
                is ListadoUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onReintentar) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is ListadoUiState.Contenido -> {
                    val actividades = uiState.actividades
                    val urgentes = ReglasActividad.actividadesUrgentes(actividades).size
                    val promedio = ReglasActividad.promedioProgreso(actividades).toInt()
                    val completadas = actividades.count { it.progreso >= 100 }

                    val resumen = buildString {
                        appendLine("Urgentes: $urgentes")
                        appendLine("Promedio: $promedio%")
                        appendLine("Completadas: $completadas")
                        appendLine("Total actividades: ${actividades.size}")
                    }

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
                                value = textoBusqueda,
                                onValueChange = onBusquedaChange,
                                label = { Text("Buscar actividad...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        items(actividades, key = { it.id }) { actividad ->
                            TarjetaActividad(actividad = actividad, onClick = { onActividadClick(actividad.id) })
                        }

                        item { Spacer(modifier = Modifier.height(20.dp)) }
                        item { SeccionAgile() }
                    }
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
private fun EstadoVacio(modifier: Modifier = Modifier, onCrearClick: () -> Unit = {}) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onCrearClick) {
                Text("Crear Actividad")
            }
        }
    }
}

@Composable
private fun EstadoVacioAprendiz(modifier: Modifier = Modifier) {
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
                text = "No hay actividades disponibles",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Espera a que tu instructor asigne nuevas actividades.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(name = "Actividades normales", showBackground = true)
@Composable
private fun PantallaActividadesPreview() {
    MiFormacionCTMATheme {
        PantallaActividades(uiState = ListadoUiState.Contenido(ActividadesDemo.listaInicial))
    }
}

@Preview(name = "Actividades anchas", showBackground = true, widthDp = 700)
@Composable
private fun PantallaActividadesPreviewAncha() {
    MiFormacionCTMATheme {
        ContenidoAdaptable(uiState = ListadoUiState.Contenido(ActividadesDemo.listaInicial))
    }
}

@Preview(name = "Estado vacío", showBackground = true)
@Composable
private fun EstadoVacioPreview() {
    MiFormacionCTMATheme {
        EstadoVacio()
    }
}