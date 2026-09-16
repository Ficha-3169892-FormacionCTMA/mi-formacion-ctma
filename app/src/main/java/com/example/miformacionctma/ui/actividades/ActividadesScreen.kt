package com.example.miformacionctma.ui.actividades

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.model.ActividadFormativa

@Composable
fun ActividadesScreen(
    viewModel: ActividadViewModel,
    modifier: Modifier = Modifier
) {
    // Recolección consciente del ciclo de vida (Caso CA-02 / CA-05)
    val listadoState by viewModel.listadoUiState.collectAsStateWithLifecycle()
    val queryBusqueda by viewModel.queryBusqueda.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo de búsqueda reactiva con debounce de 300ms
        OutlinedTextField(
            value = queryBusqueda,
            onValueChange = { viewModel.actualizarBusqueda(it) },
            label = { Text("Buscar actividades...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Renderizado reactivo según la interfaz sellada ListadoUiState
        when (val state = listadoState) {
            is ListadoUiState.Cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ListadoUiState.Vacio -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron actividades.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is ListadoUiState.Contenido -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.actividades) { actividad ->
                        ActividadItem(actividad = actividad)
                    }
                }
            }
            is ListadoUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadItem(actividad: ActividadFormativa) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = actividad.titulo,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = actividad.descripcion,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}