package com.example.miformacionctma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.MiFormacionApplication
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.screens.PantallaActividades
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModelFactory


@Composable
fun MiFormacionAppNav(
    application: MiFormacionApplication
) {
    val navController = rememberNavController()

    val viewModel: ActividadesViewModel = viewModel(
        factory = ActividadesViewModelFactory(
            application.actividadRepository,
            application.preferenciasRepository
        )
    )
    val ordenarPorPrioridad by viewModel.ordenarPorPrioridad.collectAsState()
    val actividades by viewModel.actividadesOrdenadas.collectAsState()
    val competencias by viewModel.competencias.collectAsState()

    // Estado del formulario de creación preservado en rotaciones
    var formTitulo by rememberSaveable { mutableStateOf("") }
    var formDescripcion by rememberSaveable { mutableStateOf("") }
    var formFecha by rememberSaveable { mutableStateOf("") }
    var formPrioridad by rememberSaveable { mutableStateOf(Prioridad.MEDIA) }
    var formCompetenciaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var formProgreso by rememberSaveable { mutableIntStateOf(0) }

    // Computación de errores usando ReglasActividad
    val tituloError = ReglasActividad.validarTitulo(formTitulo)
    val descripcionError = ReglasActividad.validarDescripcion(formDescripcion)
    val fechaError = ReglasActividad.validarFecha(formFecha)

    // Banderas de estado para interacción
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
        competenciaId = formCompetenciaId,
        progreso = formProgreso
    )

    NavHost(
        navController = navController,
        startDestination = Destino.Lista.ruta
    ) {
        // Destino 1 - Listado de actividades
        composable(Destino.Lista.ruta) {
            PantallaActividades(
                actividades = actividades,
                ordenarPorPrioridad = ordenarPorPrioridad,
                onOrdenPorPrioridadChange = viewModel::guardarOrdenPorPrioridad,
                onActividadClick = { id ->
                    navController.navigate(Destino.Detalle.crearRuta(id)) {
                        launchSingleTop = true
                    }
                },
                onCrearClick = {
                    navController.navigate(Destino.Crear.ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Destino 2 - Formulario de creación
        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = uiStateFormulario,
                competencias = competencias,
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
                onCompetenciaChange = { competenciaId ->
                    formCompetenciaId = competenciaId
                },
                onProgresoChange = { formProgreso = it },
                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {
                        val nuevaActividad = ActividadFormativa(
                            id = (actividades.maxOfOrNull { it.id } ?: 0L) + 1L,
                            titulo = formTitulo.trim(),
                            descripcion = formDescripcion.trim(),
                            fecha = formFecha.trim(),
                            progreso = formProgreso,
                            competenciaId = formCompetenciaId,
                            prioridad = formPrioridad
                        )

                        viewModel.insertar(nuevaActividad)

                        // Limpiar formulario y reiniciar las banderas de interacción
                        formTitulo = ""
                        formDescripcion = ""
                        formFecha = ""
                        formPrioridad = Prioridad.MEDIA
                        formCompetenciaId = null
                        formProgreso = 0

                        formTituloTocado = false
                        formDescripcionTocado = false
                        formFechaTocado = false

                        navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // Destino 3 - Detalle de actividad
        composable(
            route = Destino.Detalle.ruta,
            arguments = listOf(
                navArgument("actividadId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L

            val resultado by produceState<Pair<ActividadFormativa, String?>?>(initialValue = null, id) {
                value = viewModel.obtenerConCompetencia(id)
            }

            PantallaDetalleActividad(
                actividadId = id,
                actividades = actividades,
                competenciaNombre = resultado?.second,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}
