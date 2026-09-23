package com.example.miformacionctma.ui.actividades

import com.example.miformacionctma.model.ActividadFormativa

// Estado de la lista principal de actividades (Consulta)
sealed interface ListadoUiState {
    data object Cargando : ListadoUiState
    data object Vacio : ListadoUiState
    data class Contenido(val actividades: List<ActividadFormativa>) : ListadoUiState
    data class Error(val mensaje: String) : ListadoUiState
}

// Estado de operaciones de escritura (Creación, edición o eliminación)
sealed interface OperacionUiState {
    data object Inactiva : OperacionUiState
    data object EnCurso : OperacionUiState
    data object Exitosa : OperacionUiState
    data class Fallida(val mensaje: String) : OperacionUiState
}

// Estado de la sincronización con Supabase (se muestra en un Snackbar con opción de reintentar)
sealed interface SincronizacionUiState {
    data object Inactiva : SincronizacionUiState
    data object EnCurso : SincronizacionUiState
    data class Fallida(val mensaje: String) : SincronizacionUiState
}

// Evidencia lista para mostrar en la UI (la pantalla no depende de la entidad de Room)
data class EvidenciaUi(
    val rutaLocal: String,
    val tipo: String,
    val tamanoKb: Long,
    val estado: EstadoEvidencia
)

enum class EstadoEvidencia { PENDIENTE, SINCRONIZADA, FALLIDA }
