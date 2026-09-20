package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.components.neonTextFieldColors
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.state.OperacionUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaCrearActividad(
    uiState: FormularioActividadUiState,
    operacionUiState: OperacionUiState = OperacionUiState.Inactiva,
    esEdicion: Boolean = false,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onProgresoChange: (Int) -> Unit,
    estudiantesDisponibles: List<com.example.miformacionctma.model.Usuario> = emptyList(),
    estudianteSeleccionadoId: String? = null,
    estudiantesSeleccionadosSet: Set<String> = emptySet(),
    onEstudianteToggle: (String) -> Unit = {},
    onSelectAllEstudiantes: () -> Unit = {},
    onGuardarClick: () -> Unit,
    onVolver: () -> Unit
) {
    val guardando = operacionUiState is OperacionUiState.EnCurso

    androidx.compose.runtime.LaunchedEffect(operacionUiState) {
        if (operacionUiState is OperacionUiState.Exitosa) {
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (esEdicion) "Editar Actividad" else "Nueva Actividad",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver, enabled = !guardando) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = onTituloChange,
                label = { Text("Título de la actividad *") },
                isError = uiState.tituloTocado && uiState.tituloError != null,
                supportingText = {
                    if (uiState.tituloTocado && uiState.tituloError != null) {
                        Text(text = uiState.tituloError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${uiState.titulo.length}/80 caracteres")
                    }
                },
                singleLine = true,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción") },
                isError = uiState.descripcionTocado && uiState.descripcionError != null,
                supportingText = {
                    if (uiState.descripcionTocado && uiState.descripcionError != null) {
                        Text(text = uiState.descripcionError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${uiState.descripcion.length}/240 caracteres")
                    }
                },
                minLines = 3,
                maxLines = 5,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = uiState.fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha límite (AAAA-MM-DD) *") },
                isError = uiState.fechaTocado && uiState.fechaError != null,
                supportingText = {
                    if (uiState.fechaTocado && uiState.fechaError != null) {
                        Text(text = uiState.fechaError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Text(text = "Prioridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Prioridad.entries.forEach { prioridadEnum ->
                    FilterChip(
                        selected = uiState.prioridad == prioridadEnum,
                        onClick = { onPrioridadChange(prioridadEnum) },
                        label = { Text(prioridadEnum.name) }
                    )
                }
            }

            if (estudiantesDisponibles.isNotEmpty() && !esEdicion) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Asignar Estudiantes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onSelectAllEstudiantes) {
                        Text(if (estudiantesSeleccionadosSet.size == estudiantesDisponibles.size) "Deseleccionar todos" else "Seleccionar todos")
                    }
                }
                
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    estudiantesDisponibles.forEach { estudiante ->
                        FilterChip(
                            selected = estudiantesSeleccionadosSet.contains(estudiante.id),
                            onClick = { onEstudianteToggle(estudiante.id) },
                            label = { Text(estudiante.nombreCompleto ?: estudiante.email) }
                        )
                    }
                }
            }

            Column {
                Text(text = "Progreso: ${uiState.progreso}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Slider(
                    value = uiState.progreso.toFloat(),
                    onValueChange = { onProgresoChange(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = !guardando
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (guardando) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Guardando...")
                }
            }

            Button(
                onClick = onGuardarClick,
                enabled = uiState.puedeGuardar && !guardando,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (guardando) "PROCESANDO..." else "GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
            }
        }
    }
}
