package com.example.miformacionctma.ui.state

import com.example.miformacionctma.model.Usuario

sealed interface AuthUiState {
    object Invitado : AuthUiState
    object Cargando : AuthUiState
    data class Autenticado(val usuario: Usuario) : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
}
