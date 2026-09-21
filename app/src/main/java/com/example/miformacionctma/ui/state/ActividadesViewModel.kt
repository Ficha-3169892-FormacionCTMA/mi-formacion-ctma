package com.example.miformacionctma.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.ActividadEntity
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.repository.ActividadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Representa los estados de la operación de actualización desde la red.
 */
sealed interface RefreshUiState {
    data object Idle : RefreshUiState
    data object Running : RefreshUiState
    data object Success : RefreshUiState
    data class Failed(val message: String) : RefreshUiState
}

/**
 * ViewModel que coordina la observación de datos locales desde Room
 * y la sincronización asíncrona con el servicio web.
 */
class ActividadesViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    // 1. Observa el flujo de Room como fuente canónica de datos
    val actividades: StateFlow<List<ActividadEntity>> = repository.observeActividades()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Estado separado para comunicar el progreso de la sincronización remota
    private val _refreshState = MutableStateFlow<RefreshUiState>(RefreshUiState.Idle)
    val refreshState: StateFlow<RefreshUiState> = _refreshState.asStateFlow()

    init {
        // Al iniciar, intenta sincronizar con el servidor web
        refresh()
    }

    /**
     * Inicia una actualización desde la red en el scope del ViewModel.
     * Es cancelable y no bloquea la interfaz de usuario.
     */
    fun refresh() {
        viewModelScope.launch {
            _refreshState.value = RefreshUiState.Running

            repository.refresh()
                .onSuccess {
                    _refreshState.value = RefreshUiState.Success
                }
                .onFailure { throwable ->
                    val userMessage = when (throwable) {
                        is NetworkFailure -> mapErrorToUserMessage(throwable.error)
                        else -> "Ocurrió un error inesperado al actualizar"
                    }
                    _refreshState.value = RefreshUiState.Failed(userMessage)
                }
        }
    }

    /**
     * Mapea los errores técnicos de red a mensajes amables para la persona usuaria.
     */
    private fun mapErrorToUserMessage(error: DataError): String {
        return when (error) {
            is DataError.NoConnection -> "Sin conexión a internet. Se muestran datos guardados."
            is DataError.Timeout -> "La actualización tardó demasiado. Por favor, reintenta."
            is DataError.Unauthorized -> "Tu sesión ha expirado. Inicia sesión nuevamente."
            is DataError.NotFound -> "El recurso solicitado no fue encontrado."
            is DataError.Server -> "El servicio no está disponible en este momento (Código ${error.code})."
            is DataError.InvalidPayload -> "No se pudo procesar la respuesta del servidor."
            is DataError.Unknown -> "No fue posible conectar con el servidor."
        }
    }
}