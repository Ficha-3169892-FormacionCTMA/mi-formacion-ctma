package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.AuthRepository
import com.example.miformacionctma.model.Rol
import com.example.miformacionctma.ui.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Invitado)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            if (repository.estaAutenticado()) {
                val usuario = repository.obtenerUsuarioActual()
                if (usuario != null) {
                    _uiState.value = AuthUiState.Autenticado(usuario)
                } else {
                    _uiState.value = AuthUiState.Invitado
                }
            } else {
                _uiState.value = AuthUiState.Invitado
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            repository.login(email, password)
                .onSuccess { usuario ->
                    _uiState.value = AuthUiState.Autenticado(usuario)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun signup(email: String, password: String, nombre: String, rol: Rol) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            repository.signup(email, password, nombre, rol)
                .onSuccess { usuario ->
                    _uiState.value = AuthUiState.Autenticado(usuario)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Error al registrarse")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Invitado
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Invitado
    }
}
