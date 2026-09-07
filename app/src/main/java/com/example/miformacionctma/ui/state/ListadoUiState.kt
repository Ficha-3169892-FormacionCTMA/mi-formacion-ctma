package com.example.miformacionctma.ui.state
import com.example.miformacionctma.model.ActividadFormativa

sealed interface ListadoUiState {

    data object Cargando : ListadoUiState

    data object Vacio : ListadoUiState

    data class Contenido(
        val actividades: List<ActividadFormativa>
    ) : ListadoUiState

    data class Error(
        val mensaje: String
    ) : ListadoUiState
}
