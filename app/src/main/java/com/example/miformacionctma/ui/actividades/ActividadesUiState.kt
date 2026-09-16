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