package com.example.miformacionctma.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.MiFormacionApplication
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.screens.PantallaActividadesRoute
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModel
import com.example.miformacionctma.ui.viewmodel.ActividadesViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MiFormacionAppNav(
    application: MiFormacionApplication
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val viewModel: ActividadesViewModel = viewModel(
        factory = ActividadesViewModelFactory(
            application.actividadRepository,
            application.preferenciasRepository
        )
    )
    val ordenarPorPrioridad by viewModel.ordenarPorPrioridad.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoBusqueda by viewModel.textoBusqueda.collectAsStateWithLifecycle()
    val competencias by viewModel.competencias.collectAsStateWithLifecycle()

    val listaActividades = (uiState as? ListadoUiState.Contenido)?.actividades ?: emptyList()

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

    var idActividadEdicion by rememberSaveable { mutableStateOf<Long?>(null) }

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
            PantallaActividadesRoute(
                viewModel = viewModel,
                onActividadClick = { id ->
                    navController.navigate(Destino.Detalle.crearRuta(id)) {
                        launchSingleTop = true
                    }
                },
                onCrearClick = {
                    idActividadEdicion = null
                    formTitulo = ""
                    formDescripcion = ""
                    formFecha = ""
                    formPrioridad = Prioridad.MEDIA
                    formCompetenciaId = null
                    formProgreso = 0
                    
                    formTituloTocado = false
                    formDescripcionTocado = false
                    formFechaTocado = false

                    navController.navigate(Destino.Crear.ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Destino 2 - Formulario de creación/edición
        composable(Destino.Crear.ruta) {
            PantallaCrearActividad(
                uiState = uiStateFormulario,
                competencias = competencias,
                esEdicion = idActividadEdicion != null,
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
                        val fechaInstant = try {
                            LocalDate.parse(formFecha.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                        } catch (e: Exception) {
                            Instant.now()
                        }

                        val nuevaActividad = ActividadFormativa(
                            id = idActividadEdicion ?: ((listaActividades.maxOfOrNull { it.id } ?: 0L) + 1L),
                            titulo = formTitulo.trim(),
                            descripcion = formDescripcion.trim(),
                            fecha = fechaInstant,
                            progreso = formProgreso,
                            competenciaId = formCompetenciaId,
                            prioridad = formPrioridad,
                            completada = formProgreso >= 100
                        )

                        if (idActividadEdicion != null) {
                            viewModel.actualizar(nuevaActividad)
                        } else {
                            viewModel.insertar(nuevaActividad)
                        }

                        formTitulo = ""
                        formDescripcion = ""
                        formFecha = ""
                        formPrioridad = Prioridad.MEDIA
                        formCompetenciaId = null
                        formProgreso = 0
                        idActividadEdicion = null

                        formTituloTocado = false
                        formDescripcionTocado = false
                        formFechaTocado = false

                        navController.popBackStack()
                    }
                },
                onVolver = {
                    idActividadEdicion = null
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

            val resultado by produceState<Pair<ActividadFormativa, String?>?>(
                initialValue = null,
                id
            ) {
                value = viewModel.obtenerConCompetencia(id)
            }

            val evidenciaEntity by remember(id) {
                viewModel.observarEvidencia(id)
            }.collectAsStateWithLifecycle(initialValue = null)

            // URL pública remota de fallback en Supabase Storage
            val baseUrlStorage = BuildConfig.SUPABASE_URL.replace("/rest/v1/", "/").trimEnd('/')
            val remoteUriFallback = Uri.parse("$baseUrlStorage/storage/v1/object/public/evidencias/evidencia_$id.jpg")
            
            val uriFinal = evidenciaEntity?.localUri?.let { Uri.parse(it) } ?: remoteUriFallback

            PantallaDetalleActividad(
                actividadId = id,
                actividades = listaActividades,
                competenciaNombre = resultado?.second,
                evidenciaUri = uriFinal,
                estadoSincronizacion = evidenciaEntity?.estado?.name ?: "SINCRONIZADA",
                onGuardarEvidencia = { uri, mime, tamano ->
                    viewModel.guardarEvidenciaYSubir(context, id, uri, mime, tamano)
                },
                onReintentarSubida = {
                    viewModel.reintentarSubirEvidencia(context, id)
                },
                onEliminarEvidencia = {
                    viewModel.eliminarEvidencia(context, id)
                },
                onVolver = {
                    navController.popBackStack()
                },
                onEditar = { actividad ->
                    idActividadEdicion = actividad.id
                    formTitulo = actividad.titulo
                    formDescripcion = actividad.descripcion
                    formFecha = DateTimeFormatter.ISO_LOCAL_DATE
                        .withZone(ZoneId.systemDefault())
                        .format(actividad.fecha)
                    formPrioridad = actividad.prioridad
                    formCompetenciaId = actividad.competenciaId
                    formProgreso = actividad.progreso
                    
                    navController.navigate(Destino.Crear.ruta)
                },
                onEliminar = { actividad ->
                    viewModel.eliminar(actividad)
                    navController.popBackStack()
                }
            )
        }
    }
}
