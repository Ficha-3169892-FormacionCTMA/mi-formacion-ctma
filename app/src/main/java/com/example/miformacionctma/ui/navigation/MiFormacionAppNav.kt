package com.example.miformacionctma.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.ui.actividades.SincronizacionUiState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.ListadoUiState
import com.example.miformacionctma.ui.screens.PantallaActividades
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.state.FormularioActividadUiState

@Composable
fun MiFormacionAppNav(
    viewModel: ActividadViewModel
) {
    val navController = rememberNavController()

    // Estado reactivo de la base de datos (emite List<ActividadFormativa>)
    val listadoState by viewModel.listadoUiState.collectAsStateWithLifecycle()

    val queryBusqueda by viewModel.queryBusqueda.collectAsStateWithLifecycle()
    val soloCompletadas by viewModel.soloCompletadas.collectAsStateWithLifecycle()
    val sincronizacionState by viewModel.sincronizacionUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Los errores de sincronización se muestran en un Snackbar con opción de reintentar
    LaunchedEffect(sincronizacionState) {
        val fallo = sincronizacionState as? SincronizacionUiState.Fallida ?: return@LaunchedEffect
        val resultado = snackbarHostState.showSnackbar(
            message = fallo.mensaje,
            actionLabel = "Reintentar",
            duration = SnackbarDuration.Long
        )
        viewModel.descartarErrorSincronizacion()
        if (resultado == SnackbarResult.ActionPerformed) viewModel.sincronizarConServidor()
    }

    // Lista completa (sin búsqueda ni filtros) para el detalle y la edición
    val actividadesFormativas by viewModel.todasLasActividades.collectAsStateWithLifecycle()

    // Estado del formulario preservado en rotaciones
    var formTitulo by rememberSaveable { mutableStateOf("") }
    var formDescripcion by rememberSaveable { mutableStateOf("") }
    var formFecha by rememberSaveable { mutableStateOf("") }
    var formPrioridad by rememberSaveable { mutableStateOf(Prioridad.MEDIA) }
    var formProgreso by rememberSaveable { mutableIntStateOf(0) }

    // Computación de errores de validación
    val tituloError = ReglasActividad.validarTitulo(formTitulo)
    val descripcionError = ReglasActividad.validarDescripcion(formDescripcion)
    val fechaError = ReglasActividad.validarFecha(formFecha)

    var formTituloTocado by rememberSaveable { mutableStateOf(false) }
    var formDescripcionTocado by rememberSaveable { mutableStateOf(false) }
    var formFechaTocado by rememberSaveable { mutableStateOf(false) }

    val uiStateFormulario = FormularioActividadUiState(
        titulo = formTitulo,
        tituloError = tituloError,
        tituloTocado = formTituloTocado,
        descripcion = formDescripcion,
        descripcionError = descripcionError,
        descripcionTocado = formDescripcionTocado,
        fecha = formFecha,
        fechaError = fechaError,
        fechaTocado = formFechaTocado,
        prioridad = formPrioridad,
        progreso = formProgreso
    )

    NavHost(
        navController = navController,
        startDestination = Destino.Lista.ruta
    ) {
        // Destino 1 - Listado Principal
        composable(Destino.Lista.ruta) {
            val limpiarYCrear = {
                // Limpiar formulario al crear una nueva actividad
                formTitulo = ""
                formDescripcion = ""
                formFecha = ""
                formPrioridad = Prioridad.MEDIA
                formProgreso = 0
                formTituloTocado = false
                formDescripcionTocado = false
                formFechaTocado = false

                navController.navigate(Destino.Crear.ruta) {
                    launchSingleTop = true
                }
            }

            when (val estado = listadoState) {
                is ListadoUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ListadoUiState.Contenido, is ListadoUiState.Vacio -> {
                    PantallaActividades(
                        actividades = (estado as? ListadoUiState.Contenido)?.actividades ?: emptyList(),
                        queryBusqueda = queryBusqueda,
                        onQueryChange = viewModel::actualizarBusqueda,
                        soloCompletadas = soloCompletadas,
                        onSoloCompletadasChange = viewModel::cambiarFiltroCompletadas,
                        snackbarHostState = snackbarHostState,
                        onActividadClick = { id ->
                            navController.navigate(Destino.Detalle.crearRuta(id)) {
                                launchSingleTop = true
                            }
                        },
                        onCrearClick = limpiarYCrear
                    )
                }
                is ListadoUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error de base de datos: ${estado.mensaje}")
                        Button(
                            onClick = viewModel::reintentarCarga,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        // Destino 2 - Formulario de Creación
        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = uiStateFormulario,
                onTituloChange = {
                    formTitulo = it
                    formTituloTocado = true
                },
                onDescripcionChange = {
                    formDescripcion = it
                    formDescripcionTocado = true
                },
                onFechaChange = {
                    formFecha = it
                    formFechaTocado = true
                },
                onPrioridadChange = { formPrioridad = it },
                onProgresoChange = { formProgreso = it },
                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {
                        val nuevaActividad = ActividadFormativa(
                            id = System.currentTimeMillis(),
                            titulo = formTitulo,
                            descripcion = formDescripcion,
                            fecha = formFecha,
                            prioridad = formPrioridad,
                            progreso = formProgreso,
                            diasRestantes = 0
                        )

                        viewModel.agregarActividad(nuevaActividad)

                        // Reset del formulario
                        formTitulo = ""
                        formDescripcion = ""
                        formFecha = ""
                        formPrioridad = Prioridad.MEDIA
                        formProgreso = 0

                        formTituloTocado = false
                        formDescripcionTocado = false
                        formFechaTocado = false

                        navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                },
                tituloPantalla = "Crear Actividad"
            )
        }

        // Destino 3 - Detalle de la actividad
        composable(
            route = Destino.Detalle.ruta,
            arguments = listOf(
                navArgument("actividadId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            PantallaDetalleActividad(
                actividadId = id,
                actividades = actividadesFormativas,
                viewModel = viewModel,
                onVolver = {
                    navController.popBackStack()
                },
                onEliminarClick = { idEliminar ->
                    val actividadAEliminar = actividadesFormativas.find { it.id == idEliminar }

                    if (actividadAEliminar != null) {
                        viewModel.eliminarActividad(actividadAEliminar)
                        navController.popBackStack()
                    }
                },
                onEditarClick = { idEditar ->
                    navController.navigate(Destino.Editar.crearRuta(idEditar)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Destino 4 - Formulario de Edición
        composable(
            route = Destino.Editar.ruta,
            arguments = listOf(
                navArgument("actividadId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val idEditar = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            val actividadExistente = actividadesFormativas.find { it.id == idEditar }

            // Precargar los datos de la actividad en los campos del formulario
            LaunchedEffect(actividadExistente) {
                actividadExistente?.let {
                    formTitulo = it.titulo
                    formDescripcion = it.descripcion
                    formFecha = ReglasActividad.fechaParaFormulario(it.fecha)
                    formPrioridad = it.prioridad
                    formProgreso = it.progreso
                    formTituloTocado = false
                    formDescripcionTocado = false
                    formFechaTocado = false
                }
            }

            PantallaCrearActividad(
                uiState = uiStateFormulario,
                onTituloChange = {
                    formTitulo = it
                    formTituloTocado = true
                },
                onDescripcionChange = {
                    formDescripcion = it
                    formDescripcionTocado = true
                },
                onFechaChange = {
                    formFecha = it
                    formFechaTocado = true
                },
                onPrioridadChange = { formPrioridad = it },
                onProgresoChange = { formProgreso = it },
                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar && actividadExistente != null) {
                        val actividadActualizada = actividadExistente.copy(
                            titulo = formTitulo,
                            descripcion = formDescripcion,
                            fecha = formFecha,
                            prioridad = formPrioridad,
                            progreso = formProgreso
                        )

                        viewModel.actualizarActividad(actividadActualizada)
                        navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                },
                tituloPantalla = "Editar Actividad"
            )
        }
    }
}