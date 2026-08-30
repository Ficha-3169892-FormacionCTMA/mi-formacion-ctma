package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.FormularioActividadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearActividad(
    uiState: FormularioActividadUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onProgresoChange: (Int) -> Unit,
    onGuardarClick: () -> Unit,
    onVolver: () -> Unit
) {
    var enProcesoGuardado by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CAMPO: TÍTULO
            val mostrarErrorTitulo = uiState.tituloTocado && uiState.tituloError != null
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = onTituloChange,
                label = { Text("Título de la actividad *") },
                placeholder = { Text("Ej: Taller de Kotlin") },
                isError = mostrarErrorTitulo,
                supportingText = {
                    if (mostrarErrorTitulo) {
                        Text(text = uiState.tituloError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${uiState.titulo.length}/80 caracteres")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // CAMPO: DESCRIPCIÓN
            val mostrarErrorDescripcion = uiState.descripcionTocado && uiState.descripcionError != null
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Agrega detalles sobre la actividad") },
                isError = mostrarErrorDescripcion,
                supportingText = {
                    if (mostrarErrorDescripcion) {
                        Text(text = uiState.descripcionError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${uiState.descripcion.length}/240 caracteres")
                    }
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // CAMPO: FECHA
            val mostrarErrorFecha = uiState.fechaTocado && uiState.fechaError != null
            OutlinedTextField(
                value = uiState.fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha límite *") },
                placeholder = { Text("Año-Mes-Día (Ej: 2026-09-15)") },
                isError = mostrarErrorFecha,
                supportingText = {
                    if (mostrarErrorFecha) {
                        Text(text = uiState.fechaError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // CAMPO: PRIORIDAD
            Text(
                text = "Prioridad",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Prioridad.entries.forEach { prioridadEnum ->
                    FilterChip(
                        selected = (uiState.prioridad == prioridadEnum),
                        onClick = { onPrioridadChange(prioridadEnum) },
                        label = {
                            Text(
                                when (prioridadEnum) {
                                    Prioridad.BAJA -> "Baja"
                                    Prioridad.MEDIA -> "Media"
                                    Prioridad.ALTA -> "Alta"
                                }
                            )
                        }
                    )
                }
            }

            // CAMPO: PROGRESO
            Column {
                Text(
                    text = "Progreso inicial: ${uiState.progreso}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = uiState.progreso.toFloat(),
                    onValueChange = { onProgresoChange(it.toInt()) },
                    valueRange = 0f..100f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN GUARDAR
            Button(
                onClick = {
                    if (!enProcesoGuardado && uiState.puedeGuardar) {
                        enProcesoGuardado = true
                        onGuardarClick()
                    }
                },
                enabled = uiState.puedeGuardar && !enProcesoGuardado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (enProcesoGuardado) "Guardando..." else "Guardar Actividad")
            }
        }
    }
}
