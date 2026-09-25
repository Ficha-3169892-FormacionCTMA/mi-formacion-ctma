package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.ui.viewmodel.ProfileDto
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.FormularioActividadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearActividad(
    uiState: FormularioActividadUiState,
    listaCompetencias: List<CompetenciaEntity>,
    listaAprendices: List<ProfileDto>,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onProgresoChange: (Int) -> Unit,
    onCompetenciaChange: (Long) -> Unit,
    onAprendizChange: (String?) -> Unit,
    onGuardarClick: () -> Unit,
    onVolver: () -> Unit
) {
    var enProcesoGuardado by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val nombreAprendizSeleccionado = listaAprendices.find { it.id == uiState.aprendizId }?.nombre ?: "Seleccionar aprendiz (Opcional)"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Actividad") },
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
                        Text(text = uiState.tituloError ?: "", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${uiState.titulo.length}/80 caracteres")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // CAMPO: DESCRIPCIÓN
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Agrega detalles sobre la actividad") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // 🧑‍🎓 CAMPO: ASIGNAR APRENDIZ ESPECÍFICO POR NOMBRE
            Text(
                text = "Asignar a Aprendiz",
                style = MaterialTheme.typography.titleMedium
            )
            ExposedDropdownMenuBox(
                expanded = expandedMenu,
                onExpandedChange = { expandedMenu = !expandedMenu }
            ) {
                OutlinedTextField(
                    value = nombreAprendizSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true) // 👈 Corregido el warning de deprecación
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("-- Ninguno (General para todos) --") },
                        onClick = {
                            onAprendizChange(null)
                            expandedMenu = false
                        }
                    )
                    listaAprendices.forEach { aprendiz ->
                        DropdownMenuItem(
                            text = { Text(aprendiz.nombre) },
                            onClick = {
                                onAprendizChange(aprendiz.id)
                                expandedMenu = false
                            }
                        )
                    }
                }
            }

            // CAMPO: SELECCIÓN DE COMPETENCIA
            Text(
                text = "Competencia *",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (listaCompetencias.isEmpty()) {
                    Text("Cargando competencias...", style = MaterialTheme.typography.bodySmall)
                } else {
                    listaCompetencias.forEach { competencia ->
                        FilterChip(
                            selected = (uiState.competenciaId == competencia.id),
                            onClick = { onCompetenciaChange(competencia.id) },
                            label = { Text(competencia.nombre) }
                        )
                    }
                }
            }

            // CAMPO: FECHA
            OutlinedTextField(
                value = uiState.fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha límite *") },
                placeholder = { Text("Año-Mes-Día (Ej: 2026-09-15)") },
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