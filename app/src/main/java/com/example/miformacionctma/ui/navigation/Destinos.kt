package com.example.miformacionctma.ui.navigation

sealed class Destino(val ruta: String) {
    data object Lista : Destino("lista_screen")
    data object Crear : Destino("crear_screen")
    data object Detalle : Destino("detalle_screen/{actividadId}") {
        fun crearRuta(actividadId: Long) = "detalle_screen/$actividadId"
    }
    data object Editar : Destino("editar_screen/{actividadId}") {
        fun crearRuta(actividadId: Long) = "editar_screen/$actividadId"
    }
    data object Evidencias : Destino("evidencias_screen/{actividadId}") {
        fun crearRuta(actividadId: Long) = "evidencias_screen/$actividadId"
    }
}
