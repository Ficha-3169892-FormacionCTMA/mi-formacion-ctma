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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.components.TarjetaActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel

@Composable
fun ActividadesRoute(
    viewModel: ActividadesViewModel,
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()

    when (val state = uiState) {

        ListadoUiState.Cargando -> {
            EstadoCargando()
        }

        ListadoUiState.Vacio -> {
            EstadoVacio(
                onCrearClick = onCrearClick
            )
        }

        is ListadoUiState.Contenido -> {
            ContenidoAdaptable(
                actividades = state.actividades,
                busqueda = busqueda,
                onBuscar = viewModel::cambiarBusqueda,
                onActividadClick = onActividadClick,
                onCrearClick = onCrearClick
            )
        }

        is ListadoUiState.Error -> {
            EstadoError(
                mensaje = state.mensaje,
                onReintentar = viewModel::reintentar
            )
        }
    }
}

@Composable
fun ContenidoAdaptable(
    actividades: List<ActividadFormativa>,
    modifier: Modifier = Modifier,
    busqueda: String = "",
    onBuscar: (String) -> Unit = {},
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
                    busqueda = busqueda,
                    onBuscar = onBuscar,
                    onActividadClick = onActividadClick,
                    onCrearClick = onCrearClick
                )

            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        actividades,
                        key = { it.id }
                    ) { actividad ->

                        TarjetaActividad(
                            actividad = actividad,
                            onClick = {
                                onActividadClick(
                                    actividad.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaActividades(
    actividades: List<ActividadFormativa>,
    busqueda: String = "",
    onBuscar: (String) -> Unit = {},
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {}
) {
    val urgentes =
        ReglasActividad
            .actividadesUrgentes(actividades)
            .size

    val promedio =
        ReglasActividad
            .promedioProgreso(actividades)
            .toInt()

    val completadas =
        actividades.count {
            it.progreso >= 100
        }

    val resumen = buildString {
        appendLine("Urgentes: $urgentes")
        appendLine("Promedio: $promedio%")
        appendLine("Completadas: $completadas")
        appendLine(
            "Total actividades: ${actividades.size}"
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCrearClick
                ) {
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

                item {
                    SeccionPresentacion(
                        resumen = resumen
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }

                item {
                    EncabezadoActividades()
                }

                item {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = onBuscar,
                        label = {
                            Text(
                                "Buscar actividad..."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (actividades.isEmpty()) {

                    item {
                        Text(
                            text = if (busqueda.isBlank()) {
                                "No hay actividades registradas."
                            } else {
                                "No se encontraron coincidencias para \"$busqueda\""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(
                                vertical = 16.dp
                            )
                        )
                    }

                } else {

                    items(
                        actividades,
                        key = { it.id }
                    ) { actividad ->

                        TarjetaActividad(
                            actividad = actividad,
                            onClick = {
                                onActividadClick(
                                    actividad.id
                                )
                            }
                        )
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

                item {
                    SeccionAgile()
                }
            }
        }
    }
}

@Composable
private fun EncabezadoActividades() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cargando actividades...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ocurrió un error",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = onReintentar
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EstadoVacio(
    onCrearClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                text = "Agrega una actividad para comenzar.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onCrearClick
            ) {
                Text("Crear actividad")
            }
        }
    }
}

@Preview(
    name = "Actividades normales",
    showBackground = true
)
@Composable
private fun PantallaActividadesPreview() {
    MiFormacionCTMATheme {

        var busqueda by rememberSaveable {
            mutableStateOf("")
        }

        PantallaActividades(
            actividades = ActividadesDemo.listaInicial,
            busqueda = busqueda,
            onBuscar = {
                busqueda = it
            }
        )
    }
}

@Preview(
    name = "Actividades anchas",
    showBackground = true,
    widthDp = 700
)
@Composable
private fun PantallaActividadesPreviewAncha() {
    MiFormacionCTMATheme {
        ContenidoAdaptable(
            actividades = ActividadesDemo.listaInicial
        )
    }
}

@Preview(
    name = "Estado vacío",
    showBackground = true
)
@Composable
private fun EstadoVacioPreview() {
    MiFormacionCTMATheme {
        EstadoVacio(
            onCrearClick = {}
        )
    }
}