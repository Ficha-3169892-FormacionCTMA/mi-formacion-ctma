package com.example.miformacionctma.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.data.local.crearDatabase
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.RoomActividadRepository
import com.example.miformacionctma.data.repository.dataStore
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.screens.ContenidoAdaptable
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.screens.PantallaLogin
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun MiFormacionAppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val appContext = context.applicationContext

    val database = remember { crearDatabase(appContext) }
    val repoActividades = remember { RoomActividadRepository(database.actividadDao()) }
    val repoPreferencias = remember { PreferenciasRepository(appContext.dataStore) }

    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ActividadesViewModel(
                    repository = repoActividades,
                    preferenciasRepository = repoPreferencias,
                    competenciaDao = database.competenciaDao()
                ) as T
            }
        }
    }

    val viewModel: ActividadesViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoBusqueda by viewModel.textoBusqueda.collectAsStateWithLifecycle()
    val operacionState by viewModel.operacion.collectAsStateWithLifecycle()
    val esInstructor by viewModel.esInstructor.collectAsStateWithLifecycle()

    val formularioState by viewModel.formularioState.collectAsStateWithLifecycle()
    val competencias by viewModel.competencias.collectAsStateWithLifecycle()
    val listaAprendices by viewModel.aprendices.collectAsStateWithLifecycle() // 👈 Recolectado correctamente

    LaunchedEffect(operacionState) {
        when (val op = operacionState) {
            is OperacionUiState.Fallida -> {
                Toast.makeText(context, "Error: ${op.mensaje}", Toast.LENGTH_LONG).show()
                viewModel.reiniciarOperacion()
            }
            is OperacionUiState.Exitosa -> {
                viewModel.reiniciarOperacion()
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Destino 0 - Pantalla de Login
        composable("login") {
            var errorLogin by remember { mutableStateOf<String?>(null) }

            PantallaLogin(
                onLoginClick = { email, password ->
                    viewModel.iniciarSesion(email, password) { exito, mensaje ->
                        if (exito) {
                            navController.navigate(Destino.Lista.ruta) {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorLogin = mensaje ?: "Credenciales incorrectas"
                        }
                    }
                },
                errorMessage = errorLogin
            )
        }

        // Destino 1 - Listado de actividades
        composable(Destino.Lista.ruta) {
            ContenidoAdaptable(
                uiState = uiState,
                textoBusqueda = textoBusqueda,
                onBusquedaChange = viewModel::cambiarBusqueda,
                onActividadClick = { id ->
                    navController.navigate(Destino.Detalle.crearRuta(id)) {
                        launchSingleTop = true
                    }
                },
                onCrearClick = {
                    navController.navigate(Destino.Crear.ruta) {
                        launchSingleTop = true
                    }
                },
                onReintentar = { viewModel.cambiarBusqueda(textoBusqueda) },
                esInstructor = esInstructor,
                onCerrarSesion = {
                    viewModel.cerrarSesion {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Destino 2 - Formulario de creación
        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = formularioState,
                listaCompetencias = competencias,
                listaAprendices = listaAprendices, // 👈 Pasado
                onTituloChange = { viewModel.actualizarTitulo(it) },
                onDescripcionChange = { viewModel.actualizarDescripcion(it) },
                onFechaChange = { viewModel.actualizarFecha(it) },
                onPrioridadChange = { viewModel.actualizarPrioridad(it) },
                onProgresoChange = { viewModel.actualizarProgreso(it) },
                onCompetenciaChange = { viewModel.actualizarCompetenciaSeleccionada(it) },
                onAprendizChange = { viewModel.actualizarAprendizSeleccionado(it) }, // 👈 Pasado
                onGuardarClick = {
                    if (formularioState.puedeGuardar) {
                        val maxId = System.currentTimeMillis().toString()
                        val nuevaActividad = ActividadFormativa(
                            id = maxId,
                            titulo = formularioState.titulo.trim(),
                            descripcion = formularioState.descripcion.trim(),
                            fecha = formularioState.fecha.trim(),
                            progreso = formularioState.progreso,
                            diasRestantes = 7,
                            prioridad = formularioState.prioridad,
                            competenciaId = formularioState.competenciaId
                        )
                        viewModel.guardar(nuevaActividad)
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
                navArgument("actividadId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("actividadId") ?: ""
            val listaActividades = when (val s = uiState) {
                is ListadoUiState.Contenido -> s.actividades
                else -> emptyList()
            }
            PantallaDetalleActividad(
                actividadId = id,
                actividades = listaActividades,
                viewModel = viewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}