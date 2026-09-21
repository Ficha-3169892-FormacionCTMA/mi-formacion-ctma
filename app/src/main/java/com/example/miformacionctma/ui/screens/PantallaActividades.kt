package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.model.Rol
import com.example.miformacionctma.ui.components.SeccionAgile
import com.example.miformacionctma.ui.components.SeccionPresentacion
import com.example.miformacionctma.ui.components.TarjetaActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel

@Composable
fun ActividadesRoute(
    viewModel: ActividadesViewModel,
    userRole: Rol = Rol.ESTUDIANTE,
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val operacion by viewModel.operacion.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refrescarDesdeServidor()
    }

    when (val state = uiState) {
        ListadoUiState.Cargando -> {
            EstadoCargando()
        }
        ListadoUiState.Vacio -> {
            EstadoVacio(
                userRole = userRole,
                onCrearClick = onCrearClick,
                onLogout = onLogout
            )
        }
        is ListadoUiState.Contenido -> {
            PantallaActividades(
                actividades = state.actividades,
                userRole = userRole,
                busqueda = busqueda,
                onBuscar = viewModel::cambiarBusqueda,
                onActividadClick = onActividadClick,
                onCrearClick = onCrearClick,
                onActualizar = viewModel::refrescarDesdeServidor,
                actualizando = operacion is OperacionUiState.EnCurso,
                operacion = operacion,
                onLogout = onLogout
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividades(
    actividades: List<ActividadFormativa>,
    userRole: Rol = Rol.ESTUDIANTE,
    busqueda: String = "",
    onBuscar: (String) -> Unit = {},
    onActividadClick: (Long) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onActualizar: () -> Unit = {},
    actualizando: Boolean = false,
    operacion: OperacionUiState = OperacionUiState.Inactiva,
    onLogout: () -> Unit = {}
) {
    val actividadesProcesadas = remember(actividades, userRole, busqueda) {
        if (userRole == Rol.INSTRUCTOR) {
            actividades.groupBy { it.titulo.trim().lowercase() }.map { (_, grupo) ->
                val principal = grupo.first()
                val promedioProgreso = grupo.map { it.progreso }.average().toInt()
                
                // Contar cuántos tienen estudiante asignado en este grupo
                val cantidadEstudiantes = grupo.filter { !it.estudianteId.isNullOrBlank() }.size
                
                principal.copy(
                    progreso = promedioProgreso,
                    estudianteNombre = if (cantidadEstudiantes > 1) "$cantidadEstudiantes estudiantes" else principal.estudianteNombre
                )
            }
        } else {
            actividades
        }
    }

    val urgentes = ReglasActividad.actividadesUrgentes(actividadesProcesadas).size
    val promedio = ReglasActividad.promedioProgreso(actividadesProcesadas).toInt()
    val completadas = actividadesProcesadas.count { it.progreso >= 100 }

    val resumen = buildString {
        appendLine("Urgentes: $urgentes")
        appendLine("Promedio: $promedio%")
        appendLine("Completadas: $completadas")
        appendLine("Total actividades: ${actividadesProcesadas.size}")
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "MI FORMACIÓN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Text(
                                "Salir",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                if (userRole == Rol.INSTRUCTOR) {
                    FloatingActionButton(
                        onClick = onCrearClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Crear")
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SeccionPresentacion(resumen = resumen)
                }
                item {
                    EncabezadoActividades(userRole)
                }
                item {
                    OutlinedButton(
                        onClick = onActualizar,
                        enabled = !actualizando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(
                                if (actualizando) "ACTUALIZANDO..." else "REFRESCAR DATOS",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                when (operacion) {
                    is OperacionUiState.Exitosa -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Datos actualizados: ${operacion.fechaActualizacion}",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    is OperacionUiState.Fallida -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = operacion.mensaje,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    else -> Unit
                }

                item {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = onBuscar,
                        placeholder = { Text("Buscar actividad...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (actividadesProcesadas.isEmpty()) {
                    item {
                        Text(
                            text = if (busqueda.isBlank()) "No hay actividades registradas." else "Sin resultados para \"$busqueda\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(actividadesProcesadas, key = { it.id }) { actividad ->
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

@Composable
private fun EncabezadoActividades(rol: Rol) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = if (rol == Rol.INSTRUCTOR) "Panel de Instructor" else "Mis Actividades",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gestiona y supervisa el progreso educativo.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun EstadoCargando() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EstadoVacio(userRole: Rol, onCrearClick: () -> Unit, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Sin actividades", style = MaterialTheme.typography.titleMedium)
            if (userRole == Rol.INSTRUCTOR) {
                Button(onClick = onCrearClick) {
                    Text("Crear primera actividad")
                }
            }
            TextButton(onClick = onLogout) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun EstadoError(mensaje: String, onReintentar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Ocurrió un error", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Text(text = mensaje, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Button(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}
