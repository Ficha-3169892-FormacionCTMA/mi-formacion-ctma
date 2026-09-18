package com.example.miformacionctma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.data.local.AppDatabase
import com.example.miformacionctma.data.repository.ActividadRepositoryImpl
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.PreferenciasRepositoryImpl
import com.example.miformacionctma.data.repository.dataStore
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.screens.ActividadesRoute
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModelFactory
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import com.example.miformacionctma.data.remote.RetrofitInstance
import com.example.miformacionctma.data.remote.RemoteEvidenciaDataSource
import com.example.miformacionctma.data.repository.EvidenciaRepositoryImpl
import com.example.miformacionctma.ui.screens.EvidenciaScreen
import com.example.miformacionctma.ui.viewmodel.EvidenciaViewModel
import com.example.miformacionctma.ui.viewmodel.EvidenciaViewModelFactory

@Composable
fun MiFormacionAppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context)
    }

    val remote = remember {
        RemoteActividadDataSource(
            RetrofitInstance.api
        )
    }

    val repository = remember(database) {
        ActividadRepositoryImpl(
            database.actividadDao(),
            remote
        )
    }

    val preferenciasRepository: PreferenciasRepository = remember(context) {
        PreferenciasRepositoryImpl(context.dataStore)
    }

    val actividadesViewModel: ActividadesViewModel = viewModel(
        factory = ActividadesViewModelFactory(
            repository = repository,
            preferenciasRepository = preferenciasRepository
        )
    )

    // Setup for Evidencias
    val remoteEvidencia = remember {
        RemoteEvidenciaDataSource(
            RetrofitInstance.storageApi,
            RetrofitInstance.evidenciaApi
        )
    }
    val evidenciaRepository = remember(database) {
        EvidenciaRepositoryImpl(context, database.evidenciaDao(), remoteEvidencia)
    }
    val evidenciaViewModel: EvidenciaViewModel = viewModel(
        factory = EvidenciaViewModelFactory(evidenciaRepository)
    )

    val actividadesState by actividadesViewModel.uiState
        .collectAsStateWithLifecycle()

    val operacionState by actividadesViewModel.operacion
        .collectAsStateWithLifecycle()

    val actividades = when (val state = actividadesState) {
        is ListadoUiState.Contenido -> state.actividades
        else -> emptyList()
    }

    var formTitulo by rememberSaveable {
        mutableStateOf("")
    }

    var formDescripcion by rememberSaveable {
        mutableStateOf("")
    }

    var formFecha by rememberSaveable {
        mutableStateOf("")
    }

    var formPrioridad by rememberSaveable {
        mutableStateOf(Prioridad.MEDIA)
    }

    var formProgreso by rememberSaveable {
        mutableIntStateOf(0)
    }

    var formTituloTocado by rememberSaveable {
        mutableStateOf(false)
    }

    var formDescripcionTocado by rememberSaveable {
        mutableStateOf(false)
    }

    var formFechaTocado by rememberSaveable {
        mutableStateOf(false)
    }

    val tituloError =
        ReglasActividad.validarTitulo(formTitulo)

    val descripcionError =
        ReglasActividad.validarDescripcion(formDescripcion)

    val fechaError =
        ReglasActividad.validarFecha(formFecha)

    val uiStateFormulario =
        FormularioActividadUiState(
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

        composable(Destino.Lista.ruta) {
            ActividadesRoute(
                viewModel = actividadesViewModel,
                onActividadClick = { id ->
                    navController.navigate(
                        Destino.Detalle.crearRuta(id)
                    ) {
                        launchSingleTop = true
                    }
                },
                onCrearClick = {
                    actividadesViewModel.limpiarOperacion()
                    formTitulo = ""
                    formDescripcion = ""
                    formFecha = ""
                    formPrioridad = Prioridad.MEDIA
                    formProgreso = 0
                    formTituloTocado = false
                    formDescripcionTocado = false
                    formFechaTocado = false
                    navController.navigate(
                        Destino.Crear.ruta
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = uiStateFormulario,
                operacionUiState = operacionState,

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

                onPrioridadChange = {
                    formPrioridad = it
                },

                onProgresoChange = {
                    formProgreso = it
                },

                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {

                        val nuevaActividad =
                            ActividadFormativa(
                                id = 0L,
                                titulo = formTitulo.trim(),
                                descripcion = formDescripcion.trim(),
                                fecha = formFecha.trim(),
                                progreso = formProgreso,
                                diasRestantes = 7,
                                prioridad = formPrioridad
                            )

                        actividadesViewModel.guardar(
                            nuevaActividad
                        )
                    }
                },

                onVolver = {
                    actividadesViewModel.limpiarOperacion()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destino.Detalle.ruta,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val id =
                backStackEntry.arguments
                    ?.getLong("actividadId")
                    ?: -1L

            PantallaDetalleActividad(
                actividadId = id,
                actividades = actividades,
                onVolver = {
                    actividadesViewModel.limpiarOperacion()
                    navController.popBackStack()
                },
                onEditarClick = { editarId ->
                    actividadesViewModel.limpiarOperacion()
                    val act = actividades.find { it.id == editarId }
                    if (act != null) {
                        formTitulo = act.titulo
                        formDescripcion = act.descripcion
                        formFecha = act.fecha
                        formPrioridad = act.prioridad
                        formProgreso = act.progreso
                        formTituloTocado = false
                        formDescripcionTocado = false
                        formFechaTocado = false
                    }
                    navController.navigate(Destino.Editar.crearRuta(editarId)) {
                        launchSingleTop = true
                    }
                },
                onEliminarClick = { eliminarId ->
                    actividadesViewModel.limpiarOperacion()
                    actividadesViewModel.eliminar(eliminarId)
                    navController.popBackStack()
                },
                onEvidenciaClick = { actividadId ->
                    navController.navigate(Destino.Evidencias.crearRuta(actividadId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Destino.Editar.ruta,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L

            PantallaCrearActividad(
                uiState = uiStateFormulario,
                operacionUiState = operacionState,
                esEdicion = true,

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

                onPrioridadChange = {
                    formPrioridad = it
                },

                onProgresoChange = {
                    formProgreso = it
                },

                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {
                        val actividadEditada = ActividadFormativa(
                            id = id,
                            titulo = formTitulo.trim(),
                            descripcion = formDescripcion.trim(),
                            fecha = formFecha.trim(),
                            progreso = formProgreso,
                            diasRestantes = 7,
                            prioridad = formPrioridad
                        )
                        actividadesViewModel.guardar(actividadEditada)
                    }
                },

                onVolver = {
                    actividadesViewModel.limpiarOperacion()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destino.Evidencias.ruta,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            EvidenciaScreen(
                actividadId = id,
                viewModel = evidenciaViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
