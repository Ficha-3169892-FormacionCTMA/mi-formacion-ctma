package com.example.miformacionctma.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.data.local.AppDatabase
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import com.example.miformacionctma.data.remote.RemoteEvidenciaDataSource
import com.example.miformacionctma.data.remote.RetrofitInstance
import com.example.miformacionctma.data.repository.ActividadRepositoryImpl
import com.example.miformacionctma.data.repository.AuthRepositoryImpl
import com.example.miformacionctma.data.repository.EvidenciaRepositoryImpl
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.PreferenciasRepositoryImpl
import com.example.miformacionctma.data.repository.dataStore
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.model.Rol
import com.example.miformacionctma.ui.screens.ActividadesRoute
import com.example.miformacionctma.ui.screens.EvidenciaScreen
import com.example.miformacionctma.ui.screens.LoginScreen
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.screens.RegisterScreen
import com.example.miformacionctma.ui.state.AuthUiState
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModelFactory
import com.example.miformacionctma.ui.viewmodel.AuthViewModel
import com.example.miformacionctma.ui.viewmodel.AuthViewModelFactory
import com.example.miformacionctma.ui.viewmodel.EvidenciaViewModel
import com.example.miformacionctma.ui.viewmodel.EvidenciaViewModelFactory
import kotlinx.coroutines.flow.first

@Composable
fun MiFormacionAppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember(context) { AppDatabase.getInstance(context) }
    val preferenciasRepository: PreferenciasRepository = remember(context) {
        PreferenciasRepositoryImpl(context.dataStore)
    }

    // Auth setup
    val authRepository = remember { AuthRepositoryImpl(RetrofitInstance.authApi, preferenciasRepository) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    var currentUserRole by remember { mutableStateOf(Rol.ESTUDIANTE) }
    var currentUserId by remember { mutableStateOf("") }
    var startRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = preferenciasRepository.accessToken.first()
        val rol = preferenciasRepository.userRole.first()
        val id = preferenciasRepository.userId.first()
        
        if (token != null) {
            currentUserRole = rol?.let { Rol.valueOf(it) } ?: Rol.ESTUDIANTE
            currentUserId = id ?: ""
            startRoute = Destino.Lista.ruta
        } else {
            startRoute = Destino.Login.ruta
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Autenticado) {
            val user = (authState as AuthUiState.Autenticado).usuario
            currentUserRole = user.rol
            currentUserId = user.id
        }
    }

    // Activities setup
    val remote = remember { RemoteActividadDataSource(RetrofitInstance.api) }
    val repository = remember(database) { ActividadRepositoryImpl(database.actividadDao(), remote) }
    val actividadesViewModel: ActividadesViewModel = viewModel(factory = ActividadesViewModelFactory(repository, preferenciasRepository))

    // Evidencias setup
    val remoteEvidencia = remember { RemoteEvidenciaDataSource(RetrofitInstance.storageApi, RetrofitInstance.evidenciaApi) }
    val evidenciaRepository = remember(database) { EvidenciaRepositoryImpl(context, database.evidenciaDao(), remoteEvidencia) }
    val evidenciaViewModel: EvidenciaViewModel = viewModel(factory = EvidenciaViewModelFactory(evidenciaRepository))

    val actividadesState by actividadesViewModel.uiState.collectAsStateWithLifecycle()
    val operacionState by actividadesViewModel.operacion.collectAsStateWithLifecycle()
    val actividades = when (val state = actividadesState) {
        is ListadoUiState.Contenido -> state.actividades
        else -> emptyList()
    }

    // Formulario states
    var formTitulo by rememberSaveable { mutableStateOf("") }
    var formDescripcion by rememberSaveable { mutableStateOf("") }
    var formFecha by rememberSaveable { mutableStateOf("") }
    var formPrioridad by rememberSaveable { mutableStateOf(Prioridad.MEDIA) }
    var formProgreso by rememberSaveable { mutableStateOf(0) }
    var formTituloTocado by rememberSaveable { mutableStateOf(false) }
    var formDescripcionTocado by rememberSaveable { mutableStateOf(false) }
    var formFechaTocado by rememberSaveable { mutableStateOf(false) }
    var estudiantesSeleccionados by rememberSaveable { mutableStateOf(setOf<String>()) }
    var listaEstudiantes by remember { mutableStateOf<List<com.example.miformacionctma.model.Usuario>>(emptyList()) }

    LaunchedEffect(currentUserRole) {
        if (currentUserRole == Rol.INSTRUCTOR) {
            listaEstudiantes = authRepository.obtenerListaEstudiantes()
        }
    }

    fun limpiarFormulario() {
        actividadesViewModel.limpiarOperacion()
        formTitulo = ""; formDescripcion = ""; formFecha = ""
        formPrioridad = Prioridad.MEDIA; formProgreso = 0
        formTituloTocado = false; formDescripcionTocado = false; formFechaTocado = false
        estudiantesSeleccionados = emptySet()
    }

    val uiStateFormulario = FormularioActividadUiState(
        titulo = formTitulo,
        tituloError = ReglasActividad.validarTitulo(formTitulo),
        tituloTocado = formTituloTocado,
        descripcion = formDescripcion,
        descripcionError = ReglasActividad.validarDescripcion(formDescripcion),
        descripcionTocado = formDescripcionTocado,
        fecha = formFecha,
        fechaError = ReglasActividad.validarFecha(formFecha),
        fechaTocado = formFechaTocado,
        prioridad = formPrioridad,
        progreso = formProgreso
    )

    if (startRoute == null) return

    NavHost(navController = navController, startDestination = startRoute!!) {
        composable(Destino.Login.ruta) {
            LoginScreen(
                uiState = authState,
                onLogin = authViewModel::login,
                onNavigateToRegister = { navController.navigate(Destino.Registro.ruta) },
                onSuccess = {
                    navController.navigate(Destino.Lista.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destino.Registro.ruta) {
            RegisterScreen(
                uiState = authState,
                onRegister = authViewModel::signup,
                onNavigateToLogin = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Destino.Lista.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destino.Lista.ruta) {
            ActividadesRoute(
                viewModel = actividadesViewModel,
                userRole = currentUserRole,
                onActividadClick = { id -> navController.navigate(Destino.Detalle.crearRuta(id)) },
                onCrearClick = {
                    limpiarFormulario()
                    navController.navigate(Destino.Crear.ruta)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Destino.Login.ruta) { popUpTo(0) }
                }
            )
        }

        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = uiStateFormulario,
                operacionUiState = operacionState,
                onTituloChange = { formTitulo = it; formTituloTocado = true },
                onDescripcionChange = { formDescripcion = it; formDescripcionTocado = true },
                onFechaChange = { formFecha = it; formFechaTocado = true },
                onPrioridadChange = { formPrioridad = it },
                onProgresoChange = { formProgreso = it },
                estudiantesDisponibles = listaEstudiantes,
                estudiantesSeleccionadosSet = estudiantesSeleccionados,
                onEstudianteToggle = { id ->
                    estudiantesSeleccionados = if (estudiantesSeleccionados.contains(id)) estudiantesSeleccionados - id else estudiantesSeleccionados + id
                },
                onSelectAllEstudiantes = {
                    estudiantesSeleccionados = if (estudiantesSeleccionados.size == listaEstudiantes.size) emptySet() else listaEstudiantes.map { it.id }.toSet()
                },
                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {
                        val ids = if (estudiantesSeleccionados.isEmpty()) listOf(null) else estudiantesSeleccionados.toList()
                        ids.forEach { estId ->
                            actividadesViewModel.guardar(
                                ActividadFormativa(
                                    id = 0L, titulo = formTitulo.trim(), descripcion = formDescripcion.trim(),
                                    fecha = formFecha.trim(), progreso = formProgreso, diasRestantes = 7,
                                    prioridad = formPrioridad, instructorId = currentUserId, estudianteId = estId
                                )
                            )
                        }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destino.Detalle.ruta,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            PantallaDetalleActividad(
                actividadId = id, actividades = actividades, userRole = currentUserRole,
                onVolver = { navController.popBackStack() },
                onEditarClick = { editarId ->
                    actividadesViewModel.limpiarOperacion()
                    val act = actividades.find { it.id == editarId }
                    if (act != null) {
                        formTitulo = act.titulo; formDescripcion = act.descripcion; formFecha = act.fecha
                        formPrioridad = act.prioridad; formProgreso = act.progreso
                        estudiantesSeleccionados = act.estudianteId?.let { setOf(it) } ?: emptySet()
                    }
                    navController.navigate(Destino.Editar.crearRuta(editarId))
                },
                onEliminarClick = { eliminarId ->
                    actividadesViewModel.eliminar(eliminarId)
                    navController.popBackStack()
                },
                onEvidenciaClick = { actividadId ->
                    navController.navigate(Destino.Evidencias.crearRuta(actividadId))
                }
            )
        }

        composable(
            route = Destino.Editar.ruta,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            PantallaCrearActividad(
                uiState = uiStateFormulario, operacionUiState = operacionState, esEdicion = true,
                onTituloChange = { formTitulo = it; formTituloTocado = true },
                onDescripcionChange = { formDescripcion = it; formDescripcionTocado = true },
                onFechaChange = { formFecha = it; formFechaTocado = true },
                onPrioridadChange = { formPrioridad = it },
                onProgresoChange = { formProgreso = it },
                onGuardarClick = {
                    if (uiStateFormulario.puedeGuardar) {
                        actividadesViewModel.guardar(
                            ActividadFormativa(
                                id = id, titulo = formTitulo.trim(), descripcion = formDescripcion.trim(),
                                fecha = formFecha.trim(), progreso = formProgreso, diasRestantes = 7,
                                prioridad = formPrioridad, instructorId = currentUserId, 
                                estudianteId = estudiantesSeleccionados.firstOrNull()
                            )
                        )
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destino.Evidencias.ruta,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId") ?: -1L
            val actividadSeleccionada = actividades.find { it.id == id }
            val relacionadas = remember(actividades, actividadSeleccionada) {
                if (actividadSeleccionada != null) {
                    actividades.filter { it.titulo == actividadSeleccionada.titulo }
                } else {
                    emptyList()
                }
            }
            EvidenciaScreen(
                actividadId = id,
                viewModel = evidenciaViewModel,
                actividadesRelacionadas = relacionadas,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
