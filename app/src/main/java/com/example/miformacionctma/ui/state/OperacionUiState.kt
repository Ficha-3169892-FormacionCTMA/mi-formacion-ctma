package com.example.miformacionctma.ui.state

sealed interface OperacionUiState {

    data object Inactiva : OperacionUiState

    data object EnCurso : OperacionUiState

    data class Exitosa(
        val fechaActualizacion: String
    ) : OperacionUiState

    data class Fallida(
        val mensaje: String
    ) : OperacionUiState
}