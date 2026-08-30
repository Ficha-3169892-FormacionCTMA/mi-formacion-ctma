package com.example.miformacionctma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miformacionctma.domain.actividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.screens.ContenidoAdaptable
import com.example.miformacionctma.ui.screens.PantallaCrearActividad
import com.example.miformacionctma.ui.screens.PantallaDetalleActividad

@Composable
fun MiFormacionAppNav() {
    val navController = rememberNavController()
    var listaActividades by remember { mutableStateOf(actividadesDemo) }

    NavHost(
        navController = navController,
        startDestination = Destino.Lista.ruta
    ) {
        // Destino 1: Lista / Contenido Adaptable
        composable(route = Destino.Lista.ruta) {
            ContenidoAdaptable(
                actividades = listaActividades,
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

        // Destino 2: Formulario de Creación
        composable(route = Destino.Crear.ruta) {
            PantallaCrearActividad(
                onGuardar = { titulo, descripcion ->
                    val nuevaActividad = ActividadFormativa(
                        id = (listaActividades.size + 1).toLong(),
                        titulo = titulo,
                        descripcion = descripcion.ifBlank { null },
                        progreso = 0,
                        diasRestantes = 7,
                        prioridad = Prioridad.MEDIA
                    )
                    listaActividades = listaActividades + nuevaActividad
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // Destino 3: Detalle por ID
        composable(
            route = Destino.Detalle.ruta,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("actividadId")
            PantallaDetalleActividad(
                actividadId = id,
                actividades = listaActividades,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}